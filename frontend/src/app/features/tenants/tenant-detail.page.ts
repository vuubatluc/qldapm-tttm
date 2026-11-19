import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, switchMap } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { toErrorMessage } from '../../core/http/api-error';
import { AssignUnitFormComponent, AssignUnitSubmit } from './assign-unit-form.component';
import { TenantApi } from './tenant.api';
import {
  CONTRACT_STATUS_LABELS,
  Contract,
  ID_TYPE_LABELS,
  PortalAccount,
  TENANT_STATUS_LABELS,
  TenantDetail,
  UnlinkedTenantUser,
} from './tenant.models';

/** Chi tiết hồ sơ khách thuê + hợp đồng; BQL gán gian hàng tại đây (UC-KT-LEASING bước 5–11). */
@Component({
  selector: 'app-tenant-detail-page',
  imports: [RouterLink, FormsModule, DatePipe, DecimalPipe, AssignUnitFormComponent],
  templateUrl: './tenant-detail.page.html',
})
export class TenantDetailPage implements OnInit {
  private readonly api = inject(TenantApi);
  protected readonly auth = inject(AuthService);

  readonly id = input.required<string>();

  protected readonly idTypeLabels = ID_TYPE_LABELS;
  protected readonly tenantStatusLabels = TENANT_STATUS_LABELS;
  protected readonly contractStatusLabels = CONTRACT_STATUS_LABELS;

  protected readonly detail = signal<TenantDetail | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);
  protected readonly busy = signal(false);
  protected readonly showAssign = signal(false);
  protected readonly portalAccount = signal<PortalAccount | null>(null);
  protected readonly showLink = signal(false);
  protected readonly linkCandidates = signal<UnlinkedTenantUser[]>([]);
  protected selectedUserId = '';

  protected readonly isAdmin = computed(() => this.auth.hasAnyRole(['ADMIN']));

  ngOnInit(): void {
    this.reload();
  }

  protected reload(): void {
    this.api.get(this.id()).subscribe({
      next: (d) => this.detail.set(d),
      error: (err: unknown) => this.error.set(toErrorMessage(err)),
    });
  }

  /** Tạo hợp đồng nháp, rồi kích hoạt ngay nếu người dùng chọn. */
  protected assign(data: AssignUnitSubmit): void {
    this.run(
      () => {
        const create$ = this.api.createContract({ ...data.request, tenantId: this.id() });
        return data.activateNow
          ? create$.pipe(switchMap((c) => this.api.activateContract(c.id)))
          : create$;
      },
      (c) => {
        this.showAssign.set(false);
        return c.status === 'ACTIVE'
          ? `Đã kích hoạt hợp đồng ${c.contractNo} tại gian ${c.units.map((u) => u.unitCode).join(', ')}.`
          : `Đã lưu hợp đồng nháp ${c.contractNo}.`;
      },
    );
  }

  protected activate(c: Contract): void {
    this.run(() => this.api.activateContract(c.id), (r) => `Đã kích hoạt hợp đồng ${r.contractNo}.`);
  }

  protected cancel(c: Contract): void {
    if (confirm(`Hủy hợp đồng nháp ${c.contractNo}?`)) {
      this.run(() => this.api.cancelContract(c.id), (r) => `Đã hủy hợp đồng ${r.contractNo}.`);
    }
  }

  protected terminate(c: Contract): void {
    if (confirm(`Thanh lý hợp đồng ${c.contractNo}? Gian hàng sẽ trở về trạng thái Trống.`)) {
      this.run(() => this.api.terminateContract(c.id), (r) => `Đã thanh lý hợp đồng ${r.contractNo}.`);
    }
  }

  protected toggleStatus(): void {
    const t = this.detail()?.tenant;
    if (!t) return;
    const next = t.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.run(
      () => this.api.changeStatus(t.id, next),
      () => `Đã chuyển trạng thái sang "${this.tenantStatusLabels[next]}".`,
    );
  }

  /** Mở ô chọn tài khoản đã đăng ký để liên kết. */
  protected openLink(): void {
    this.api.unlinkedTenantUsers().subscribe({
      next: (p) => {
        this.linkCandidates.set(p.content);
        this.selectedUserId = '';
        this.showLink.set(true);
      },
      error: (err: unknown) => this.error.set(toErrorMessage(err)),
    });
  }

  protected link(): void {
    if (!this.selectedUserId) return;
    const userId = this.selectedUserId;
    this.run(
      () => this.api.linkAccount(this.id(), userId),
      (acc) => {
        this.showLink.set(false);
        return `Đã liên kết tài khoản ${acc.email}. Khách thuê có thể vào "Gian hàng của tôi" ngay.`;
      },
    );
  }

  protected unlink(email: string): void {
    if (confirm(`Hủy liên kết tài khoản ${email} khỏi hồ sơ này?`)) {
      this.run(
        () => this.api.unlinkAccount(this.id()),
        () => `Đã hủy liên kết tài khoản ${email}.`,
      );
    }
  }

  protected createPortalAccount(): void {
    this.busy.set(true);
    this.api.createPortalAccount(this.id()).subscribe({
      next: (acc) => {
        this.portalAccount.set(acc);
        this.busy.set(false);
        this.reload();
      },
      error: (err: unknown) => {
        this.error.set(toErrorMessage(err));
        this.busy.set(false);
      },
    });
  }

  private run<T>(call: () => Observable<T>, successMessage: (r: T) => string): void {
    this.busy.set(true);
    this.error.set(null);
    this.message.set(null);
    call().subscribe({
      next: (r) => {
        this.message.set(successMessage(r));
        this.busy.set(false);
        this.reload();
      },
      error: (err: unknown) => {
        this.error.set(toErrorMessage(err));
        this.busy.set(false);
        this.reload();
      },
    });
  }
}
