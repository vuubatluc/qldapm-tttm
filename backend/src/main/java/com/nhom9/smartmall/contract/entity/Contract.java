package com.nhom9.smartmall.contract.entity;

import com.nhom9.smartmall.common.entity.BaseEntity;
import com.nhom9.smartmall.tenant.entity.Tenant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Hợp đồng thuê: một khách thuê, một hoặc nhiều gian qua {@link ContractUnit}. */
@Getter
@Setter
@Entity
@Table(name = "contract")
public class Contract extends BaseEntity {

    @Column(name = "contract_no", nullable = false, unique = true, length = 40)
    private String contractNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ContractStatus status;

    @Column(name = "signed_on")
    private LocalDate signedOn;

    @Column(name = "payment_cycle_months", nullable = false)
    private short paymentCycleMonths;

    @Column(name = "deposit_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal depositAmount;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractUnit> units = new ArrayList<>();

    public void addUnit(ContractUnit cu) {
        cu.setContract(this);
        units.add(cu);
    }
}
