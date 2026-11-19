import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { roleGuard } from './auth.guards';
import { AuthService } from './auth.service';

describe('roleGuard', () => {
  const run = (role: string | null, ...allowed: Parameters<typeof roleGuard>) => {
    localStorage.clear();
    if (role) {
      localStorage.setItem(
        'smartmall.session',
        JSON.stringify({ accessToken: 'a', refreshToken: 'r', user: { role } }),
      );
    }
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient()] });
    TestBed.inject(AuthService);
    return TestBed.runInInjectionContext(() =>
      roleGuard(...allowed)({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  };

  it('allows a matching role', () => {
    expect(run('ADMIN', 'ADMIN', 'ACCOUNTANT')).toBe(true);
  });

  it('redirects other roles to /forbidden', () => {
    const result = run('TENANT', 'ADMIN');
    expect(result instanceof UrlTree).toBe(true);
    expect(String(result)).toBe('/forbidden');
  });
});
