package com.nhom9.smartmall.premises.entity;

import com.nhom9.smartmall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Gian hàng (Booth/Unit). Mã {@code unitCode} dùng chung cho hợp đồng, QR và bản đồ. */
@Getter
@Setter
@Entity
@Table(name = "unit")
public class Unit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "unit_code", nullable = false, unique = true, length = 30)
    private String unitCode;

    @Column(name = "area_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaM2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitStatus status;

    @Column(name = "planned_category", length = 100)
    private String plannedCategory;

    /** Đơn giá thuê niêm yết (VND/m²/tháng). */
    @Column(name = "ref_rent", precision = 18, scale = 2)
    private BigDecimal refRent;
}
