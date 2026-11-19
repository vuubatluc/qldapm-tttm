package com.nhom9.smartmall.contract.entity;

import java.util.Set;

/** Vòng đời hợp đồng (rút gọn từ mục 2.1 thiết kế CSDL). */
public enum ContractStatus {
    DRAFT,
    ACTIVE,
    PENDING_TERMINATION,
    TERMINATED,
    CANCELLED;

    /** Các trạng thái đang chiếm lịch thuê của gian hàng. */
    public static final Set<ContractStatus> OCCUPYING = Set.of(ACTIVE, PENDING_TERMINATION);
}
