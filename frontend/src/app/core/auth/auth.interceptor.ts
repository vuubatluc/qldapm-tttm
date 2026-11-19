import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const PUBLIC_AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/refresh'];

/** Gắn Bearer token; khi gặp 401 thì thử refresh một lần rồi gửi lại request. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const isPublic = PUBLIC_AUTH_PATHS.some((p) => req.url.includes(p));

  return next(withToken(req, isPublic ? null : auth.accessToken)).pipe(
    catchError((err: unknown) => {
      if (isPublic || !(err instanceof HttpErrorResponse) || err.status !== 401) {
        return throwError(() => err);
      }
      if (!auth.isLoggedIn()) {
        return throwError(() => err);
      }
      return auth.refresh().pipe(
        switchMap((res) => next(withToken(req, res.accessToken))),
        catchError((refreshErr: unknown) => {
          auth.logout();
          return throwError(() => refreshErr);
        }),
      );
    }),
  );
};

function withToken(req: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  return token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
}
