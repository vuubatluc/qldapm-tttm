package com.nhom9.smartmall.contract.service;

import com.nhom9.smartmall.common.exception.BusinessException;
import com.nhom9.smartmall.common.exception.ErrorCode;
import com.nhom9.smartmall.contract.dto.ContractDtos.ActiveLease;
import com.nhom9.smartmall.contract.dto.ContractDtos.ContractResponse;
import com.nhom9.smartmall.contract.dto.ContractDtos.ContractUnitRequest;
import com.nhom9.smartmall.contract.dto.ContractDtos.CreateContractRequest;
import com.nhom9.smartmall.contract.entity.Contract;
import com.nhom9.smartmall.contract.entity.ContractStatus;
import com.nhom9.smartmall.contract.entity.ContractUnit;
import com.nhom9.smartmall.contract.repository.ContractRepository;
import com.nhom9.smartmall.contract.repository.ContractUnitRepository;
import com.nhom9.smartmall.premises.entity.Unit;
import com.nhom9.smartmall.premises.entity.UnitStatus;
import com.nhom9.smartmall.premises.repository.UnitRepository;
import com.nhom9.smartmall.premises.service.PremisesService;
import com.nhom9.smartmall.tenant.entity.Tenant;
import com.nhom9.smartmall.tenant.entity.TenantStatus;
import com.nhom9.smartmall.tenant.service.TenantService;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hợp đồng thuê gian hàng: tạo nháp → kích hoạt (gán gian) → kết thúc.
 * Quy tắc chống trùng kỳ thuê theo mục 2.1 "Toàn vẹn và cập nhật đồng thời".
 */
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractUnitRepository contractUnitRepository;
    private final UnitRepository unitRepository;
    private final PremisesService premisesService;
    private final TenantService tenantService;

    @Transactional
    public ContractResponse createDraft(CreateContractRequest req) {
        Tenant tenant = tenantService.require(req.tenantId());
        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Khách thuê đã ngừng hoạt động");
        }
        Set<UUID> seen = new HashSet<>();
        Contract c = new Contract();
        c.setTenant(tenant);
        c.setStatus(ContractStatus.DRAFT);
        c.setPaymentCycleMonths(req.paymentCycleMonths());
        c.setDepositAmount(req.depositAmount());
        c.setNote(req.note());

        for (ContractUnitRequest line : req.units()) {
            if (!seen.add(line.unitId())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Một gian chỉ xuất hiện một lần trong hợp đồng");
            }
            if (!line.leaseToExclusive().isAfter(line.leaseFrom())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Ngày kết thúc phải sau ngày bắt đầu");
            }
            Unit unit = unitRepository.findById(line.unitId())
                    .orElseThrow(() -> BusinessException.notFound("gian hàng"));
            ContractUnit cu = new ContractUnit();
            cu.setUnit(unit);
            cu.setLeaseFrom(line.leaseFrom());
            cu.setLeaseToExcl(line.leaseToExclusive());
            cu.setChargeAreaM2(line.chargeAreaM2() != null ? line.chargeAreaM2() : unit.getAreaM2());
            cu.setRentRate(line.rentRate());
            cu.setServiceRate(line.serviceRate());
            c.addUnit(cu);
        }
        c.setContractNo(resolveContractNo(req.contractNo(), c));
        return ContractResponse.from(contractRepository.save(c));
    }

    /**
     * Kích hoạt hợp đồng. Trong một giao dịch: khóa các gian theo thứ tự UUID, kiểm tra trạng thái gian
     * và giao nhau kỳ thuê, rồi chuyển gian sang Đã thuê. Trùng lịch trả 409 CONTRACT_PERIOD_OVERLAP.
     */
    @Transactional
    public ContractResponse activate(UUID contractId, UUID actorId) {
        Contract c = requireDetail(contractId);
        if (c.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessException(ErrorCode.CONTRACT_INVALID_STATE);
        }
        List<ContractUnit> lines = c.getUnits().stream()
                .sorted(Comparator.comparing(cu -> cu.getUnit().getId()))
                .toList();
        LocalDate today = LocalDate.now();
        for (ContractUnit cu : lines) {
            Unit unit = unitRepository.findByIdForUpdate(cu.getUnit().getId())
                    .orElseThrow(() -> BusinessException.notFound("gian hàng"));
            boolean startsNow = !cu.getLeaseFrom().isAfter(today);
            if (startsNow && unit.getStatus() != UnitStatus.AVAILABLE && unit.getStatus() != UnitStatus.RESERVED) {
                throw new BusinessException(ErrorCode.UNIT_NOT_AVAILABLE,
                        "Gian " + unit.getUnitCode() + " không ở trạng thái Trống");
            }
            if (contractUnitRepository.existsOverlap(unit.getId(), c.getId(), cu.getLeaseFrom(), cu.getLeaseToExcl(),
                    ContractStatus.OCCUPYING)) {
                throw new BusinessException(ErrorCode.CONTRACT_PERIOD_OVERLAP,
                        "Kỳ thuê của gian " + unit.getUnitCode() + " bị trùng với hợp đồng khác");
            }
            premisesService.changeStatus(unit, startsNow ? UnitStatus.OCCUPIED : UnitStatus.RESERVED, actorId,
                    "Kích hoạt hợp đồng " + c.getContractNo());
        }
        c.setStatus(ContractStatus.ACTIVE);
        if (c.getSignedOn() == null) {
            c.setSignedOn(today);
        }
        return ContractResponse.from(c);
    }

    @Transactional
    public ContractResponse cancelDraft(UUID contractId) {
        Contract c = requireDetail(contractId);
        if (c.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessException(ErrorCode.CONTRACT_INVALID_STATE);
        }
        c.setStatus(ContractStatus.CANCELLED);
        return ContractResponse.from(c);
    }

    /**
     * Kết thúc hợp đồng và trả gian về Trống. Bản rút gọn: chưa có bước xác nhận kế toán/kỹ thuật
     * (CONTRACT_CASE) — sẽ bổ sung khi làm luồng thanh lý đầy đủ.
     */
    @Transactional
    public ContractResponse terminate(UUID contractId, UUID actorId) {
        Contract c = requireDetail(contractId);
        if (!ContractStatus.OCCUPYING.contains(c.getStatus())) {
            throw new BusinessException(ErrorCode.CONTRACT_INVALID_STATE);
        }
        List<ContractUnit> lines = c.getUnits().stream()
                .sorted(Comparator.comparing(cu -> cu.getUnit().getId()))
                .toList();
        for (ContractUnit cu : lines) {
            Unit unit = unitRepository.findByIdForUpdate(cu.getUnit().getId()).orElseThrow();
            if (unit.getStatus() == UnitStatus.OCCUPIED || unit.getStatus() == UnitStatus.RESERVED) {
                premisesService.changeStatus(unit, UnitStatus.AVAILABLE, actorId,
                        "Thanh lý hợp đồng " + c.getContractNo());
            }
        }
        c.setStatus(ContractStatus.TERMINATED);
        return ContractResponse.from(c);
    }

    @Transactional(readOnly = true)
    public ContractResponse get(UUID contractId) {
        return ContractResponse.from(requireDetail(contractId));
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> listByTenant(UUID tenantId) {
        return contractRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(ContractResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ActiveLease> activeLeasesOf(Collection<UUID> tenantIds) {
        if (tenantIds.isEmpty()) {
            return List.of();
        }
        return contractUnitRepository.findLeasesOfTenants(tenantIds, ContractStatus.OCCUPYING).stream()
                .map(cu -> new ActiveLease(cu.getContract().getTenant().getId(), cu.getUnit().getUnitCode(),
                        cu.getLeaseToExcl()))
                .toList();
    }

    private Contract requireDetail(UUID id) {
        return contractRepository.findDetailById(id).orElseThrow(() -> BusinessException.notFound("hợp đồng"));
    }

    /** Quy chuẩn gợi ý: HD-YYYY-[MãGian]-[MãKhách]. */
    private String resolveContractNo(String requested, Contract c) {
        String no = requested;
        if (no == null || no.isBlank()) {
            ContractUnit first = c.getUnits().getFirst();
            no = "HD-%d-%s-%s".formatted(first.getLeaseFrom().getYear(), first.getUnit().getUnitCode(),
                    c.getTenant().getTenantCode().replace("KT-", ""));
            String base = no;
            for (int i = 2; contractRepository.existsByContractNo(no); i++) {
                no = base + "-" + i;
            }
        } else if (contractRepository.existsByContractNo(no.trim())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Số hợp đồng đã tồn tại");
        }
        return no.trim();
    }
}
