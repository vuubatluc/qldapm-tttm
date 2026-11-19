package com.nhom9.smartmall.contract.entity;

import com.nhom9.smartmall.common.entity.BaseEntity;
import com.nhom9.smartmall.premises.entity.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Phân bổ gian hàng trong hợp đồng. Kỳ thuê theo khoảng nửa mở [leaseFrom, leaseToExcl):
 * hợp đồng hết hạn 30/06 thì leaseToExcl = 01/07.
 */
@Getter
@Setter
@Entity
@Table(name = "contract_unit")
public class ContractUnit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "lease_from", nullable = false)
    private LocalDate leaseFrom;

    @Column(name = "lease_to_excl", nullable = false)
    private LocalDate leaseToExcl;

    @Column(name = "charge_area_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal chargeAreaM2;

    /** Đơn giá thuê thỏa thuận (VND/m²/tháng). */
    @Column(name = "rent_rate", nullable = false, precision = 18, scale = 2)
    private BigDecimal rentRate;

    /** Phí dịch vụ (VND/m²/tháng). */
    @Column(name = "service_rate", nullable = false, precision = 18, scale = 2)
    private BigDecimal serviceRate;
}
