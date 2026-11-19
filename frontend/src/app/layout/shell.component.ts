import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ROLE_HOME, ROLE_LABELS, Role } from '../core/auth/auth.models';
import { AuthService } from '../core/auth/auth.service';

interface NavItem {
  label: string;
  path: string;
  roles: Role[];
}

/** Menu hiển thị theo vai trò; quyền thật vẫn do backend kiểm tra. */
const NAV_ITEMS: NavItem[] = [
  { label: 'Tổng quan', path: ROLE_HOME.ADMIN, roles: ['ADMIN'] },
  { label: 'Tổng quan', path: ROLE_HOME.ACCOUNTANT, roles: ['ACCOUNTANT'] },
  { label: 'Công việc', path: ROLE_HOME.TECHNICIAN, roles: ['TECHNICIAN'] },
  { label: 'Gian hàng của tôi', path: ROLE_HOME.TENANT, roles: ['TENANT'] },
  { label: 'Khách thuê', path: '/tenants', roles: ['ADMIN', 'ACCOUNTANT'] },
  { label: 'Tài khoản & phân quyền', path: '/users', roles: ['ADMIN'] },
];

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  protected readonly auth = inject(AuthService);
  protected readonly roleLabels = ROLE_LABELS;
  protected readonly navItems = computed(() => {
    const role = this.auth.role();
    return role ? NAV_ITEMS.filter((i) => i.roles.includes(role)) : [];
  });
}
