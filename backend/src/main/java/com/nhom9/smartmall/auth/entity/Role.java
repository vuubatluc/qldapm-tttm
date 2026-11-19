package com.nhom9.smartmall.auth.entity;

/**
 * Vai trò người dùng (RBAC - mục 1.2 tài liệu phân tích).
 * Khách mua sắm không có tài khoản nên không có vai trò.
 */
public enum Role {
    /** Ban quản lý TTTM. */
    ADMIN,
    /** Kế toán. */
    ACCOUNTANT,
    /** Kỹ thuật viên. */
    TECHNICIAN,
    /** Khách thuê (chủ gian hàng). */
    TENANT
}
