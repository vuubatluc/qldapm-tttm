import { DecimalPipe } from '@angular/common';
import { Component, inject, input, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TenantApi } from './tenant.api';
import { CreateContractRequest, Floor, Unit } from './tenant.models';

export interface AssignUnitSubmit {
  request: Omit<CreateContractRequest, 'tenantId'>;
  activateNow: boolean;
}

/**
 * Form gán gian hàng: chọn gian Trống theo tầng → hệ thống điền sẵn diện tích, đơn giá niêm yết
 * → nhập điều khoản (UC-KT-LEASING bước 5–7).
 */
@Component({
  selector: 'app-assign-unit-form',
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './assign-unit-form.component.html',
})
export class AssignUnitFormComponent {
  private readonly api = inject(TenantApi);

  readonly busy = input(false);
  readonly submitted = output<AssignUnitSubmit>();
  readonly cancelled = output<void>();

  protected readonly floors = signal<Floor[]>([]);
  protected readonly units = signal<Unit[]>([]);
  protected readonly selectedUnit = signal<Unit | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    floorId: [''],
    unitId: ['', Validators.required],
    contractNo: [''],
    leaseFrom: [today(), Validators.required],
    /** Ngày cuối cùng thuê (bao gồm); khi gửi API sẽ +1 ngày thành leaseToExclusive. */
    leaseToInclusive: [oneYearLater(), Validators.required],
    chargeAreaM2: [0, [Validators.required, Validators.min(0.01)]],
    rentRate: [0, [Validators.required, Validators.min(0)]],
    serviceRate: [0, [Validators.required, Validators.min(0)]],
    paymentCycleMonths: [1, [Validators.required, Validators.min(1), Validators.max(12)]],
    depositAmount: [0, [Validators.required, Validators.min(0)]],
    activateNow: [true],
  });

  constructor() {
    this.api.floors().subscribe((f) => this.floors.set(f));
    this.loadUnits();
    this.form.controls.floorId.valueChanges.subscribe(() => this.loadUnits());
    this.form.controls.unitId.valueChanges.subscribe((id) => this.onUnitSelected(id));
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    if (v.leaseToInclusive < v.leaseFrom) {
      this.form.controls.leaseToInclusive.setErrors({ beforeStart: true });
      return;
    }
    this.submitted.emit({
      activateNow: v.activateNow,
      request: {
        contractNo: v.contractNo.trim() || null,
        paymentCycleMonths: v.paymentCycleMonths,
        depositAmount: v.depositAmount,
        note: null,
        units: [
          {
            unitId: v.unitId,
            leaseFrom: v.leaseFrom,
            leaseToExclusive: addDays(v.leaseToInclusive, 1),
            chargeAreaM2: v.chargeAreaM2,
            rentRate: v.rentRate,
            serviceRate: v.serviceRate,
          },
        ],
      },
    });
  }

  private loadUnits(): void {
    const floorId = this.form.controls.floorId.value || undefined;
    this.api.units({ floorId, status: 'AVAILABLE' }).subscribe((p) => this.units.set(p.content));
    this.form.controls.unitId.setValue('');
  }

  /** Điền sẵn thông số gian; tiền cọc gợi ý = 3 tháng tiền thuê cơ sở. */
  private onUnitSelected(id: string): void {
    const unit = this.units().find((u) => u.id === id) ?? null;
    this.selectedUnit.set(unit);
    if (!unit) return;
    const rent = unit.refRent ?? 0;
    this.form.patchValue({
      chargeAreaM2: unit.areaM2,
      rentRate: rent,
      depositAmount: Math.round(unit.areaM2 * rent * 3),
    });
  }
}

function today(): string {
  return toIsoDate(new Date());
}

function oneYearLater(): string {
  const d = new Date();
  d.setFullYear(d.getFullYear() + 1);
  d.setDate(d.getDate() - 1);
  return toIsoDate(d);
}

function addDays(iso: string, days: number): string {
  const [y, m, d] = iso.split('-').map(Number);
  return toIsoDate(new Date(y, m - 1, d + days));
}

function toIsoDate(d: Date): string {
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
}
