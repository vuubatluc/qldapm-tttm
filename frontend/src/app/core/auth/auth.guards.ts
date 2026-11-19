import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from './auth.models';
import { AuthService } from './auth.service';

/** Yêu cầu đã đăng nhập. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.isLoggedIn()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** Chỉ cho khách (chưa đăng nhập) vào trang login/register. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isLoggedIn() ? inject(Router).parseUrl(auth.homeUrl()) : true;
};

/**
 * Kiểm tra vai trò. Dùng: `canActivate: [authGuard, roleGuard('ADMIN')]`.
 * Đây chỉ là chặn ở giao diện; backend luôn kiểm tra quyền lại.
 */
export function roleGuard(...roles: Role[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    return auth.hasAnyRole(roles) ? true : inject(Router).parseUrl('/forbidden');
  };
}
