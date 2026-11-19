package com.nhom9.smartmall.auth.entity;

public enum UserStatus {
    /** Tự đăng ký, chờ BQL duyệt. */
    PENDING,
    ACTIVE,
    /** Bị tạm dừng (nghỉ việc, khách thuê đã thanh lý...). */
    SUSPENDED
}
