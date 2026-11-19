import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-forbidden-page',
  imports: [RouterLink],
  template: `
    <div class="card">
      <h1>Không có quyền truy cập</h1>
      <p>Vai trò của bạn không được phép mở trang này.</p>
      <a class="btn" [routerLink]="auth.homeUrl()">Về trang chủ</a>
    </div>
  `,
})
export class ForbiddenPage {
  protected readonly auth = inject(AuthService);
}
