import { Component, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { toErrorMessage } from '../../core/http/api-error';
import { CITIZEN_ID_PATTERN, PHONE_PATTERN, TAX_CODE_PATTERN } from '../../shared/validation';
import { TenantApi } from './tenant.api';
import { BUSINESS_CATEGORIES, ID_TYPE_LABELS, PortalAccount, TenantIdType } from './tenant.models';

/** Thêm mới / cập nhật hồ sơ pháp lý khách thuê (UC-KT-LEASING bước 3). */
@Component({
  selector: 'app-tenant-form-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './tenant-form.page.html',
})
export class TenantFormPage implements OnInit {
  private readonly api = inject(TenantApi);
  private readonly router = inject(Router);

  /** Có id → chế độ sửa (route /tenants/:id/edit). */
  readonly id = input<string>();

  protected readonly categories = BUSINESS_CATEGORIES;
  protected readonly idTypeLabels = ID_TYPE_LABELS;
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Hiển thị mật khẩu tạm một lần sau khi cấp tài khoản. */
  protected readonly createdAccount = signal<{ tenantId: string; account: PortalAccount } | null>(
    null,
  );

  protected readonly form = inject(NonNullableFormBuilder).group({
    legalName: ['', [Validators.required, Validators.maxLength(200)]],
    brandName: ['', [Validators.required, Validators.maxLength(200)]],
    idType: ['TAX_CODE' as TenantIdType, Validators.required],
    idNumber: ['', [Validators.required, Validators.pattern(TAX_CODE_PATTERN)]],
    representative: [''],
    contactEmail: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    address: [''],
    category: [''],
    createPortalAccount: [true],
  });

  constructor() {
    this.form.controls.idType.valueChanges.subscribe((type) => {
      const pattern = type === 'TAX_CODE' ? TAX_CODE_PATTERN : CITIZEN_ID_PATTERN;
      this.form.controls.idNumber.setValidators([Validators.required, Validators.pattern(pattern)]);
      this.form.controls.idNumber.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    const id = this.id();
    if (id) {
      this.api.get(id).subscribe({
        next: ({ tenant }) =>
          this.form.patchValue({
            ...tenant,
            representative: tenant.representative ?? '',
            address: tenant.address ?? '',
            category: tenant.category ?? '',
            createPortalAccount: false,
          }),
        error: (err: unknown) => this.error.set(toErrorMessage(err)),
      });
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const req = {
      ...v,
      representative: v.representative || null,
      address: v.address || null,
      category: v.category || null,
    };
    this.submitting.set(true);
    this.error.set(null);

    const id = this.id();
    if (id) {
      this.api.update(id, req).subscribe({
        next: () => void this.router.navigate(['/tenants', id]),
        error: (err: unknown) => this.fail(err),
      });
    } else {
      this.api.create(req).subscribe({
        next: (res) => {
          if (res.portalAccount) {
            this.createdAccount.set({ tenantId: res.tenant.id, account: res.portalAccount });
            this.submitting.set(false);
          } else {
            void this.router.navigate(['/tenants', res.tenant.id]);
          }
        },
        error: (err: unknown) => this.fail(err),
      });
    }
  }

  private fail(err: unknown): void {
    this.error.set(toErrorMessage(err));
    this.submitting.set(false);
  }
}
