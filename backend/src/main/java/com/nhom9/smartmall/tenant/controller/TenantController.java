package com.nhom9.smartmall.tenant.controller;

import com.nhom9.smartmall.common.response.PageResponse;
import com.nhom9.smartmall.security.TenantScope;
import com.nhom9.smartmall.tenant.dto.TenantDtos.LinkAccountRequest;
import com.nhom9.smartmall.tenant.dto.TenantDtos.PortalAccount;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantCreatedResponse;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantDetail;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantRequest;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantResponse;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantSelfUpdateRequest;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantStatusRequest;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantSummary;
import com.nhom9.smartmall.tenant.entity.TenantStatus;
import com.nhom9.smartmall.tenant.service.TenantQueryService;
import com.nhom9.smartmall.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quản lý khách thuê gian hàng. Quyền theo ma trận RBAC:
 * BQL C/R/U, Kế toán R, Khách thuê R/U hồ sơ của mình, Kỹ thuật không truy cập.
 */
@Tag(name = "Tenants", description = "Hồ sơ khách thuê gian hàng")
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final TenantQueryService tenantQueryService;
    private final TenantScope tenantScope;

    @Operation(summary = "Tìm kiếm khách thuê")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public PageResponse<TenantSummary> search(@RequestParam(required = false) TenantStatus status,
                                              @RequestParam(required = false) String keyword,
                                              @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                              Pageable pageable) {
        return tenantQueryService.search(status, keyword, pageable);
    }

    @Operation(summary = "Thêm khách thuê (tùy chọn cấp tài khoản cổng khách thuê)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TenantCreatedResponse create(@Valid @RequestBody TenantRequest req) {
        return tenantService.create(req);
    }

    @Operation(summary = "Hồ sơ khách thuê của tài khoản đang đăng nhập")
    @GetMapping("/me")
    @PreAuthorize("hasRole('TENANT')")
    public TenantDetail me(@AuthenticationPrincipal Jwt jwt) {
        return tenantQueryService.detail(tenantScope.requireOwnTenantId(jwt));
    }

    @Operation(summary = "Khách thuê tự cập nhật thông tin liên hệ")
    @PatchMapping("/me")
    @PreAuthorize("hasRole('TENANT')")
    public TenantResponse updateMe(@Valid @RequestBody TenantSelfUpdateRequest req, @AuthenticationPrincipal Jwt jwt) {
        return tenantService.selfUpdate(tenantScope.requireOwnTenantId(jwt), req);
    }

    @Operation(summary = "Chi tiết khách thuê kèm hợp đồng")
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','TENANT')")
    public TenantDetail detail(@PathVariable UUID tenantId, @AuthenticationPrincipal Jwt jwt) {
        tenantScope.checkAccess(jwt, tenantId);
        return tenantQueryService.detail(tenantId);
    }

    @Operation(summary = "Cập nhật hồ sơ khách thuê")
    @PutMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public TenantResponse update(@PathVariable UUID tenantId, @Valid @RequestBody TenantRequest req) {
        return tenantService.update(tenantId, req);
    }

    @Operation(summary = "Ngừng / mở lại hợp tác")
    @PatchMapping("/{tenantId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public TenantResponse changeStatus(@PathVariable UUID tenantId, @Valid @RequestBody TenantStatusRequest req) {
        return tenantService.changeStatus(tenantId, req.status());
    }

    @Operation(summary = "Cấp tài khoản cổng khách thuê")
    @PostMapping("/{tenantId}/portal-account")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PortalAccount createPortalAccount(@PathVariable UUID tenantId) {
        return tenantService.createPortalAccount(tenantId);
    }

    @Operation(summary = "Liên kết tài khoản Khách thuê đã đăng ký với hồ sơ")
    @PostMapping("/{tenantId}/portal-account/link")
    @PreAuthorize("hasRole('ADMIN')")
    public PortalAccount linkAccount(@PathVariable UUID tenantId, @Valid @RequestBody LinkAccountRequest req) {
        return tenantService.linkExistingAccount(tenantId, req.userId());
    }

    @Operation(summary = "Hủy liên kết tài khoản cổng khách thuê")
    @DeleteMapping("/{tenantId}/portal-account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void unlinkAccount(@PathVariable UUID tenantId) {
        tenantService.unlinkAccount(tenantId);
    }

}
