export type TenantIdType = 'TAX_CODE' | 'CITIZEN_ID';
export type TenantStatus = 'ACTIVE' | 'INACTIVE';
export type UnitStatus = 'AVAILABLE' | 'RESERVED' | 'OCCUPIED' | 'MAINTENANCE';
export type ContractStatus = 'DRAFT' | 'ACTIVE' | 'PENDING_TERMINATION' | 'TERMINATED' | 'CANCELLED';

export const ID_TYPE_LABELS: Record<TenantIdType, string> = {
  TAX_CODE: 'Mã số thuế',
  CITIZEN_ID: 'CCCD',
};

export const TENANT_STATUS_LABELS: Record<TenantStatus, string> = {
  ACTIVE: 'Đang hợp tác',
  INACTIVE: 'Ngừng hợp tác',
};

export const UNIT_STATUS_LABELS: Record<UnitStatus, string> = {
  AVAILABLE: 'Trống',
  RESERVED: 'Đặt cọc',
  OCCUPIED: 'Đã thuê',
  MAINTENANCE: 'Đang sửa chữa',
};

export const CONTRACT_STATUS_LABELS: Record<ContractStatus, string> = {
  DRAFT: 'Nháp',
  ACTIVE: 'Hiệu lực',
  PENDING_TERMINATION: 'Chờ thanh lý',
  TERMINATED: 'Đã thanh lý',
  CANCELLED: 'Hủy',
};

export const BUSINESS_CATEGORIES = ['F&B', 'Thời trang', 'Mỹ phẩm', 'Điện máy', 'Khu vui chơi', 'Khác'];

export interface TenantRequest {
  legalName: string;
  brandName: string;
  idType: TenantIdType;
  idNumber: string;
  representative: string | null;
  contactEmail: string;
  phone: string;
  address: string | null;
  category: string | null;
  createPortalAccount: boolean;
}

export interface Tenant {
  id: string;
  tenantCode: string;
  legalName: string;
  brandName: string;
  idType: TenantIdType;
  idNumber: string;
  representative: string | null;
  contactEmail: string;
  phone: string;
  address: string | null;
  category: string | null;
  status: TenantStatus;
  createdAt: string;
}

export interface TenantSummary {
  id: string;
  tenantCode: string;
  brandName: string;
  legalName: string;
  idNumber: string;
  category: string | null;
  phone: string;
  status: TenantStatus;
  activeUnitCodes: string[];
  /** Ngày thuê cuối cùng (đã quy đổi từ ngày kết thúc loại trừ). */
  nearestLeaseEnd: string | null;
}

export interface PortalAccount {
  email: string;
  /** Chỉ có khi hệ thống vừa tạo tài khoản mới. */
  temporaryPassword?: string;
}

/** Tài khoản vai trò Khách thuê chưa gắn hồ sơ (để BQL chọn liên kết). */
export interface UnlinkedTenantUser {
  id: string;
  email: string;
  fullName: string;
  phone: string | null;
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED';
}

export interface TenantCreatedResponse {
  tenant: Tenant;
  portalAccount: PortalAccount | null;
}

export interface ContractUnit {
  unitId: string;
  unitCode: string;
  leaseFrom: string;
  leaseToExclusive: string;
  chargeAreaM2: number;
  rentRate: number;
  serviceRate: number;
}

export interface Contract {
  id: string;
  contractNo: string;
  tenantId: string;
  tenantBrandName: string;
  status: ContractStatus;
  signedOn: string | null;
  paymentCycleMonths: number;
  depositAmount: number;
  note: string | null;
  units: ContractUnit[];
}

export interface TenantDetail {
  tenant: Tenant;
  contracts: Contract[];
  /** Email tài khoản cổng khách thuê đã liên kết, null nếu chưa có. */
  portalAccountEmail: string | null;
}

export interface Unit {
  id: string;
  unitCode: string;
  floorCode: string;
  zoneCode: string;
  zoneName: string;
  areaM2: number;
  status: UnitStatus;
  plannedCategory: string | null;
  refRent: number | null;
}

export interface Floor {
  id: string;
  floorCode: string;
  name: string;
  levelNo: number;
  zones: { id: string; zoneCode: string; name: string }[];
}

export interface CreateContractRequest {
  tenantId: string;
  contractNo: string | null;
  paymentCycleMonths: number;
  depositAmount: number;
  note: string | null;
  units: {
    unitId: string;
    leaseFrom: string;
    leaseToExclusive: string;
    chargeAreaM2: number | null;
    rentRate: number;
    serviceRate: number;
  }[];
}
