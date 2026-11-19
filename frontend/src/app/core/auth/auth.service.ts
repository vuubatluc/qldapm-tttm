import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, finalize, shareReplay, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  ROLE_HOME,
  RegisterRequest,
  Role,
  TokenResponse,
  UserInfo,
} from './auth.models';

const STORAGE_KEY = 'smartmall.session';

interface Session {
  accessToken: string;
  refreshToken: string;
  user: UserInfo;
}

/**
 * Quản lý phiên đăng nhập. Trạng thái giữ bằng signal để component đọc trực tiếp.
 * Lưu ý: token đang lưu ở localStorage cho đơn giản; khi triển khai thật nên chuyển
 * refresh token sang HttpOnly cookie (UC-TK01, 3c).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly session = signal<Session | null>(readSession());
  private refreshInFlight$: Observable<TokenResponse> | null = null;

  readonly currentUser = computed(() => this.session()?.user ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly role = computed(() => this.session()?.user.role ?? null);

  get accessToken(): string | null {
    return this.session()?.accessToken ?? null;
  }

  login(req: LoginRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${this.baseUrl}/login`, req)
      .pipe(tap((res) => this.saveSession(res)));
  }

  register(req: RegisterRequest): Observable<UserInfo> {
    return this.http.post<UserInfo>(`${this.baseUrl}/register`, req);
  }

  /** Dùng chung một request refresh cho nhiều lời gọi 401 đồng thời. */
  refresh(): Observable<TokenResponse> {
    const refreshToken = this.session()?.refreshToken;
    if (!refreshToken) {
      throw new Error('No refresh token');
    }
    this.refreshInFlight$ ??= this.http
      .post<TokenResponse>(`${this.baseUrl}/refresh`, { refreshToken })
      .pipe(
        tap((res) => this.saveSession(res)),
        finalize(() => (this.refreshInFlight$ = null)),
        shareReplay(1),
      );
    return this.refreshInFlight$;
  }

  logout(): void {
    this.session.set(null);
    localStorage.removeItem(STORAGE_KEY);
    void this.router.navigate(['/login']);
  }

  hasAnyRole(roles: readonly Role[]): boolean {
    const r = this.role();
    return r !== null && roles.includes(r);
  }

  homeUrl(): string {
    const r = this.role();
    return r ? ROLE_HOME[r] : '/login';
  }

  private saveSession(res: TokenResponse): void {
    const s: Session = {
      accessToken: res.accessToken,
      refreshToken: res.refreshToken,
      user: res.user,
    };
    this.session.set(s);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
  }
}

function readSession(): Session | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}
