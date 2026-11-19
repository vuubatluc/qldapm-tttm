import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ROLE_LABELS } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';

/** Trang chủ tạm cho BQL / Kế toán / Kỹ thuật. Các module khác sẽ bổ sung nội dung ở đây. */
@Component({
  selector: 'app-role-home-page',
  imports: [RouterLink],
  template: `
    <h1>Xin chào, {{ auth.currentUser()?.fullName }}</h1>
    <div class="card">
      <p>
        Bạn đang đăng nhập với vai trò <strong>{{ roleLabel() }}</strong>.
      </p>
      @if (auth.hasAnyRole(['ADMIN', 'ACCOUNTANT'])) {
        <p><a routerLink="/tenants">Đi tới Quản lý khách thuê gian hàng →</a></p>
      }
      @if (auth.hasAnyRole(['ADMIN'])) {
        <p><a routerLink="/users">Duyệt tài khoản & phân quyền →</a></p>
      }
      @if (auth.hasAnyRole(['TECHNICIAN'])) {
        <p class="muted">Chức năng phiếu sự cố và chốt chỉ số điện nước thuộc Module 2 (chưa làm).</p>
      }
    </div>
  `,
})
export class RoleHomePage {
  protected readonly auth = inject(AuthService);
  protected readonly roleLabel = computed(() => {
    const r = this.auth.role();
    return r ? ROLE_LABELS[r] : '';
  });
}
