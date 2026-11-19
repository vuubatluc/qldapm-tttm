import { DatePipe } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { ROLE_LABELS, Role, USER_STATUS_LABELS, UserStatus } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';
import { PageResponse, toErrorMessage } from '../../core/http/api-error';

interface UserSummary {
  id: string;
  email: string;
  fullName: string;
  phone: string | null;
  role: Role;
  status: UserStatus;
  tenantId: string | null;
  lastLoginAt: string | null;
  createdAt: string;
}

/** BQL duyệt tài khoản đăng ký, gán vai trò, tạm dừng tài khoản (FR-TK-02). */
@Component({
  selector: 'app-user-admin-page',
  imports: [FormsModule, DatePipe, RouterLink],
  templateUrl: './user-admin.page.html',
})
export class UserAdminPage {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/users`;
  protected readonly auth = inject(AuthService);

  protected readonly roles = Object.keys(ROLE_LABELS) as Role[];
  protected readonly roleLabels = ROLE_LABELS;
  protected readonly statusLabels = USER_STATUS_LABELS;

  protected keyword = '';
  protected role: Role | '' = '';
  protected status: UserStatus | '' = '';

  protected readonly page = signal<PageResponse<UserSummary> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);

  constructor() {
    this.load(0);
  }

  protected load(pageIndex: number): void {
    let params = new HttpParams().set('page', pageIndex).set('size', 20);
    if (this.keyword.trim()) params = params.set('keyword', this.keyword.trim());
    if (this.role) params = params.set('role', this.role);
    if (this.status) params = params.set('status', this.status);
    this.http.get<PageResponse<UserSummary>>(this.url, { params }).subscribe({
      next: (p) => this.page.set(p),
      error: (err: unknown) => this.error.set(toErrorMessage(err)),
    });
  }

  protected changeRole(u: UserSummary, role: Role): void {
    this.patch(u, 'role', { role }, `Đã gán vai trò ${this.roleLabels[role]} cho ${u.email}.`);
  }

  protected changeStatus(u: UserSummary, status: UserStatus): void {
    this.patch(u, 'status', { status }, `Đã chuyển ${u.email} sang "${this.statusLabels[status]}".`);
  }

  private patch(u: UserSummary, path: string, body: object, okMessage: string): void {
    this.error.set(null);
    this.message.set(null);
    this.http.patch<UserSummary>(`${this.url}/${u.id}/${path}`, body).subscribe({
      next: () => {
        this.message.set(okMessage);
        this.load(this.page()?.page ?? 0);
      },
      error: (err: unknown) => {
        this.error.set(toErrorMessage(err));
        this.load(this.page()?.page ?? 0);
      },
    });
  }
}
