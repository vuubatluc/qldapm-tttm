package com.nhom9.smartmall.premises.entity;

/** Bốn trạng thái nghiệp vụ của gian hàng (mục 2.1 thiết kế CSDL). */
public enum UnitStatus {
    /** Trống. */
    AVAILABLE,
    /** Đặt cọc / giữ chỗ. */
    RESERVED,
    /** Đã thuê. */
    OCCUPIED,
    /** Đang sửa chữa. */
    MAINTENANCE
}
