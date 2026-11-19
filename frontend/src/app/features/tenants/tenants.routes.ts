import { Routes } from '@angular/router';
import { roleGuard } from '../../core/auth/auth.guards';

export const TENANT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./tenant-list.page').then((m) => m.TenantListPage),
  },
  {
    path: 'new',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () => import('./tenant-form.page').then((m) => m.TenantFormPage),
  },
  {
    path: ':id/edit',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () => import('./tenant-form.page').then((m) => m.TenantFormPage),
  },
  {
    path: ':id',
    loadComponent: () => import('./tenant-detail.page').then((m) => m.TenantDetailPage),
  },
];
