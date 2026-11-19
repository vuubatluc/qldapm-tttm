package com.nhom9.smartmall.tenant.service;

import com.nhom9.smartmall.common.response.PageResponse;
import com.nhom9.smartmall.contract.dto.ContractDtos.ActiveLease;
import com.nhom9.smartmall.contract.service.ContractService;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantDetail;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantResponse;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantSummary;
import com.nhom9.smartmall.tenant.entity.Tenant;
import com.nhom9.smartmall.tenant.entity.TenantStatus;
import com.nhom9.smartmall.tenant.repository.TenantRepository;
import com.nhom9.smartmall.user.service.UserAdminService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Truy vấn tổng hợp khách thuê + hợp đồng (tránh phụ thuộc vòng giữa TenantService và ContractService). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantQueryService {

    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final ContractService contractService;
    private final UserAdminService userAdminService;

    public PageResponse<TenantSummary> search(TenantStatus status, String keyword, Pageable pageable) {
        String kw = "%" + (keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        Page<Tenant> page = tenantRepository.search(status, kw, pageable);
        List<UUID> ids = page.getContent().stream().map(Tenant::getId).toList();
        Map<UUID, List<ActiveLease>> leases = contractService.activeLeasesOf(ids).stream()
                .collect(Collectors.groupingBy(ActiveLease::tenantId));
        return PageResponse.of(page, t -> toSummary(t, leases.getOrDefault(t.getId(), List.of())));
    }

    public TenantDetail detail(UUID tenantId) {
        Tenant t = tenantService.require(tenantId);
        return new TenantDetail(TenantResponse.from(t), contractService.listByTenant(tenantId),
                userAdminService.findLinkedEmail(tenantId));
    }

    private static TenantSummary toSummary(Tenant t, List<ActiveLease> leases) {
        LocalDate nearestEnd = leases.stream().map(ActiveLease::leaseToExclusive)
                .min(Comparator.naturalOrder()).map(d -> d.minusDays(1)).orElse(null);
        return new TenantSummary(t.getId(), t.getTenantCode(), t.getBrandName(), t.getLegalName(), t.getIdNumber(),
                t.getCategory(), t.getPhone(), t.getStatus(),
                leases.stream().map(ActiveLease::unitCode).sorted().toList(), nearestEnd);
    }
}
