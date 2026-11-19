export type Role = 'ADMIN' | 'ACCOUNTANT' | 'TECHNICIAN' | 'TENANT';
export type UserStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED';

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Ban quản lý',
  ACCOUNTANT: 'Kế toán',
  TECHNICIAN: 'Kỹ thuật viên',
  TENANT: 'Khách thuê',
};

export const USER_STATUS_LABELS: Record<UserStatus, string> = {
  PENDING: 'Chờ duyệt',
  ACTIVE: 'Hoạt động',
  SUSPENDED: 'Tạm dừng',
};

/** Trang chủ theo vai trò (UC-TK01 bước 11). */
export const ROLE_HOME: Record<Role, string> = {
  ADMIN: '/admin/dashboard',
  ACCOUNTANT: '/finance/dashboard',
  TECHNICIAN: '/technician/tasks',
  TENANT: '/portal/overview',
};

export interface UserInfo {
  id: string;
  email: string;
  fullName: string;
  phone: string | null;
  role: Role;
  status: UserStatus;
  tenantId: string | null;
}

export interface TokenResponse {
  accessToken: string;
  accessTokenExpiresAt: string;
  refreshToken: string;
  refreshTokenExpiresAt: string;
  user: UserInfo;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  phone: string | null;
  password: string;
}
