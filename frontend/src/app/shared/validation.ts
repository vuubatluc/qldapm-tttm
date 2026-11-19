/** Quy tắc kiểm tra dùng chung, phải khớp với backend (AuthDtos, TenantDtos). */
export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/;
export const PHONE_PATTERN = /^(0|\+84)\d{9,10}$/;
export const TAX_CODE_PATTERN = /^(\d{10}|\d{13})$/;
export const CITIZEN_ID_PATTERN = /^\d{12}$/;
