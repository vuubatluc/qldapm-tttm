import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { toErrorMessage } from '../../core/http/api-error';
import { TenantApi } from '../tenants/tenant.api';
import { CONTRACT_STATUS_LABELS, TenantDetail } from '../tenants/tenant.models';

/** Cổng thông tin khách thuê: xem hồ sơ và gian hàng đang thuê của mình. */
@Component({
  selector: 'app-portal-overview-page',
  imports: [DatePipe, DecimalPipe],
  template: `
    <h1>Gian hàng của tôi</h1>
    @if (error()) {
      <div class="alert alert-error">{{ error() }}</div>
    }
    @if (detail(); as d) {
      <div class="card">
        <h2 style="margin-top: 0">{{ d.tenant.brandName }} <span class="muted">· {{ d.tenant.tenantCode }}</span></h2>
        <p>{{ d.tenant.legalName }} · {{ d.tenant.contactEmail }} · {{ d.tenant.phone }}</p>
      </div>
      <h2>Hợp đồng</h2>
      <div class="table-wrap card" style="padding: 0">
        <table class="table">
          <thead>
            <tr><th>Số HĐ</th><th>Gian</th><th>Kỳ thuê</th><th>Diện tích</th><th>Đơn giá</th><th>Trạng thái</th></tr>
          </thead>
          <tbody>
            @for (c of d.contracts; track c.id) {
              @for (u of c.units; track u.unitId) {
                <tr>
                  <td>{{ c.contractNo }}</td>
                  <td>{{ u.unitCode }}</td>
                  <td>{{ u.leaseFrom | date: 'dd/MM/yyyy' }} – {{ u.leaseToExclusive | date: 'dd/MM/yyyy' }}</td>
                  <td>{{ u.chargeAreaM2 | number }} m²</td>
                  <td>{{ u.rentRate | number }} đ/m²/tháng</td>
                  <td><span class="badge" [class.ok]="c.status === 'ACTIVE'">{{ labels[c.status] }}</span></td>
                </tr>
              }
            } @empty {
              <tr><td colspan="6" class="muted">Chưa có hợp đồng.</td></tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class PortalOverviewPage {
  protected readonly labels = CONTRACT_STATUS_LABELS;
  protected readonly detail = signal<TenantDetail | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    inject(TenantApi)
      .me()
      .subscribe({
        next: (d) => this.detail.set(d),
        error: (err: unknown) => this.error.set(toErrorMessage(err)),
      });
  }
}
