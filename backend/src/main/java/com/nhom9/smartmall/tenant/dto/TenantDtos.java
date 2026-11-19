package com.nhom9.smartmall.tenant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nhom9.smartmall.contract.dto.ContractDtos.ContractResponse;
import com.nhom9.smartmall.tenant.entity.Tenant;
import com.nhom9.smartmall.tenant.entity.TenantIdType;
import com.nhom9.smartmall.tenant.entity.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class TenantDtos {

    private TenantDtos() {
    }

    /** Dùng cho cả tạo mới và cập nhật hồ sơ (BQL). */
    public record TenantRequest(
            @NotBlank @Size(max = 200) String legalName,
            @NotBlank @Size(max = 200) String brandName,
            @NotNull TenantIdType idType,
            @NotBlank @Pattern(regexp = "^\\d{10}(\\d{2,3})?$|^\\d{10}-\\d{3}$",
                    message = "MST gồm 10/13 số, CCCD gồm 12 số") String idNumber,
            @Size(max = 150) String representative,
            @NotBlank @Email @Size(max = 254) String contactEmail,
            @NotBlank @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
            String address,
            @Size(max = 100) String category,
            /** Chỉ dùng khi tạo mới: đồng thời cấp tài khoản cổng khách thuê bằng contactEmail. */
            boolean createPortalAccount) {
    }

    /** Khách thuê tự cập nhật thông tin liên hệ (RBAC: R/U hồ sơ của mình). */
    public record TenantSelfUpdateRequest(
            @Size(max = 150) String representative,
            @NotBlank @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
            String address) {
    }

    public record TenantStatusRequest(@NotNull TenantStatus status) {
    }

    public record TenantResponse(UUID id, String tenantCode, String legalName, String brandName,
                                 TenantIdType idType, String idNumber, String representative,
                                 String contactEmail, String phone, String address, String category,
                                 TenantStatus status, Instant createdAt) {

        public static TenantResponse from(Tenant t) {
            return new TenantResponse(t.getId(), t.getTenantCode(), t.getLegalName(), t.getBrandName(),
                    t.getIdType(), t.getIdNumber(), t.getRepresentative(), t.getContactEmail(), t.getPhone(),
                    t.getAddress(), t.getCategory(), t.getStatus(), t.getCreatedAt());
        }
    }

    /** Dòng trong danh sách: kèm gian đang thuê và ngày hết hạn gần nhất (UC-KT-LEASING 1a). */
    public record TenantSummary(UUID id, String tenantCode, String brandName, String legalName, String idNumber,
                                String category, String phone, TenantStatus status,
                                List<String> activeUnitCodes, LocalDate nearestLeaseEnd) {
    }

    /** portalAccountEmail = null nghĩa là chưa có tài khoản cổng khách thuê. */
    public record TenantDetail(TenantResponse tenant, List<ContractResponse> contracts, String portalAccountEmail) {
    }

    /** Liên kết một tài khoản Khách thuê đã đăng ký với hồ sơ này. */
    public record LinkAccountRequest(@NotNull UUID userId) {
    }

    public record TenantCreatedResponse(TenantResponse tenant, PortalAccount portalAccount) {
    }

    /** temporaryPassword chỉ có khi hệ thống vừa tạo tài khoản mới; null khi liên kết tài khoản có sẵn. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PortalAccount(String email, String temporaryPassword) {
    }
}
