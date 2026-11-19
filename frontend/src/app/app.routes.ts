import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/auth/auth.guards';
import { ShellComponent } from './layout/shell.component';

/** Mọi trang tính năng đều lazy-load (loadComponent) để giảm bundle ban đầu. */
export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register.page').then((m) => m.RegisterPage),
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'admin/dashboard',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/dashboard/role-home.page').then((m) => m.RoleHomePage),
      },
      {
        path: 'finance/dashboard',
        canActivate: [roleGuard('ACCOUNTANT')],
        loadComponent: () =>
          import('./features/dashboard/role-home.page').then((m) => m.RoleHomePage),
      },
      {
        path: 'technician/tasks',
        canActivate: [roleGuard('TECHNICIAN')],
        loadComponent: () =>
          import('./features/dashboard/role-home.page').then((m) => m.RoleHomePage),
      },
      {
        path: 'portal/overview',
        canActivate: [roleGuard('TENANT')],
        loadComponent: () =>
          import('./features/portal/portal-overview.page').then((m) => m.PortalOverviewPage),
      },
      {
        path: 'tenants',
        canActivate: [roleGuard('ADMIN', 'ACCOUNTANT')],
        loadChildren: () => import('./features/tenants/tenants.routes').then((m) => m.TENANT_ROUTES),
      },
      {
        path: 'users',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/users/user-admin.page').then((m) => m.UserAdminPage),
      },
      {
        path: 'forbidden',
        loadComponent: () =>
          import('./features/dashboard/forbidden.page').then((m) => m.ForbiddenPage),
      },
      { path: '', pathMatch: 'full', redirectTo: 'login' },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
