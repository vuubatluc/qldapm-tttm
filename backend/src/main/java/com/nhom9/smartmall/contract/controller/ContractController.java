package com.nhom9.smartmall.contract.controller;

import com.nhom9.smartmall.contract.dto.ContractDtos.ContractResponse;
import com.nhom9.smartmall.contract.dto.ContractDtos.CreateContractRequest;
import com.nhom9.smartmall.contract.service.ContractService;
import com.nhom9.smartmall.security.CurrentUser;
import com.nhom9.smartmall.security.TenantScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Contracts", description = "Hợp đồng thuê / gán gian hàng cho khách thuê")
@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final TenantScope tenantScope;

    @Operation(summary = "Danh sách hợp đồng của một khách thuê")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public List<ContractResponse> listByTenant(@RequestParam UUID tenantId) {
        return contractService.listByTenant(tenantId);
    }

    @Operation(summary = "Tạo hợp đồng nháp (một hoặc nhiều gian)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ContractResponse create(@Valid @RequestBody CreateContractRequest req) {
        return contractService.createDraft(req);
    }

    @Operation(summary = "Chi tiết hợp đồng")
    @GetMapping("/{contractId}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','TENANT')")
    public ContractResponse get(@PathVariable UUID contractId, @AuthenticationPrincipal Jwt jwt) {
        ContractResponse c = contractService.get(contractId);
        tenantScope.checkAccess(jwt, c.tenantId());
        return c;
    }

    @Operation(summary = "Kích hoạt hợp đồng — gán gian cho khách thuê")
    @PostMapping("/{contractId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ContractResponse activate(@PathVariable UUID contractId, @AuthenticationPrincipal Jwt jwt) {
        return contractService.activate(contractId, CurrentUser.from(jwt).id());
    }

    @Operation(summary = "Hủy hợp đồng nháp")
    @PostMapping("/{contractId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ContractResponse cancel(@PathVariable UUID contractId) {
        return contractService.cancelDraft(contractId);
    }

    @Operation(summary = "Thanh lý hợp đồng — trả gian về Trống")
    @PostMapping("/{contractId}/terminations")
    @PreAuthorize("hasRole('ADMIN')")
    public ContractResponse terminate(@PathVariable UUID contractId, @AuthenticationPrincipal Jwt jwt) {
        return contractService.terminate(contractId, CurrentUser.from(jwt).id());
    }
}
