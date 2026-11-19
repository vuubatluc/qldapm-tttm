import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { PageResponse, toErrorMessage } from '../../core/http/api-error';
import { TenantApi } from './tenant.api';
import { TENANT_STATUS_LABELS, TenantStatus, TenantSummary } from './tenant.models';

/** UC-KT-LEASING 1a: tra cứu, lọc danh sách khách thuê. */
@Component({
  selector: 'app-tenant-list-page',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './tenant-list.page.html',
})
export class TenantListPage {
  private readonly api = inject(TenantApi);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly statusLabels = TENANT_STATUS_LABELS;
  protected keyword = '';
  protected status: TenantStatus | '' = '';

  protected readonly page = signal<PageResponse<TenantSummary> | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.load(0);
  }

  protected load(pageIndex: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .search({ keyword: this.keyword.trim(), status: this.status, page: pageIndex, size: 20 })
      .subscribe({
        next: (res) => {
          this.page.set(res);
          this.loading.set(false);
        },
        error: (err: unknown) => {
          this.error.set(toErrorMessage(err));
          this.loading.set(false);
        },
      });
  }

  protected open(t: TenantSummary): void {
    void this.router.navigate(['/tenants', t.id]);
  }
}
