package com.nhom9.smartmall.contract.dto;

import com.nhom9.smartmall.contract.entity.Contract;
import com.nhom9.smartmall.contract.entity.ContractStatus;
import com.nhom9.smartmall.contract.entity.ContractUnit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ContractDtos {

    private ContractDtos() {
    }

    /** POST /contracts — tạo hợp đồng nháp (mục 4.1 thiết kế API). */
    public record CreateContractRequest(
            @NotNull UUID tenantId,
            @Size(max = 40) String contractNo,
            @NotNull @Min(1) @Max(12) Short paymentCycleMonths,
            @NotNull @DecimalMin("0") BigDecimal depositAmount,
            String note,
            @NotEmpty @Valid List<ContractUnitRequest> units) {
    }

    public record ContractUnitRequest(
            @NotNull UUID unitId,
            @NotNull LocalDate leaseFrom,
            /** Ngày ngay sau ngày thuê cuối cùng. */
            @NotNull LocalDate leaseToExclusive,
            /** Bỏ trống thì lấy diện tích gian hàng. */
            @DecimalMin(value = "0", inclusive = false) BigDecimal chargeAreaM2,
            @NotNull @DecimalMin("0") BigDecimal rentRate,
            @NotNull @DecimalMin("0") BigDecimal serviceRate) {
    }

    public record ContractUnitResponse(UUID unitId, String unitCode, LocalDate leaseFrom, LocalDate leaseToExclusive,
                                       BigDecimal chargeAreaM2, BigDecimal rentRate, BigDecimal serviceRate) {

        public static ContractUnitResponse from(ContractUnit cu) {
            return new ContractUnitResponse(cu.getUnit().getId(), cu.getUnit().getUnitCode(), cu.getLeaseFrom(),
                    cu.getLeaseToExcl(), cu.getChargeAreaM2(), cu.getRentRate(), cu.getServiceRate());
        }
    }

    public record ContractResponse(UUID id, String contractNo, UUID tenantId, String tenantBrandName,
                                   ContractStatus status, LocalDate signedOn, short paymentCycleMonths,
                                   BigDecimal depositAmount, String note, List<ContractUnitResponse> units) {

        public static ContractResponse from(Contract c) {
            return new ContractResponse(c.getId(), c.getContractNo(), c.getTenant().getId(),
                    c.getTenant().getBrandName(), c.getStatus(), c.getSignedOn(), c.getPaymentCycleMonths(),
                    c.getDepositAmount(), c.getNote(),
                    c.getUnits().stream().map(ContractUnitResponse::from).toList());
        }
    }

    /** Thông tin thuê đang hiệu lực của một khách thuê, dùng cho danh sách khách thuê. */
    public record ActiveLease(UUID tenantId, String unitCode, LocalDate leaseToExclusive) {
    }
}
