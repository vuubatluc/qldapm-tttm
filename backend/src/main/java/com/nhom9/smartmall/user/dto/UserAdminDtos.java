package com.nhom9.smartmall.user.dto;

import com.nhom9.smartmall.auth.dto.AuthDtos;
import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.entity.UserAccount;
import com.nhom9.smartmall.auth.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class UserAdminDtos {

    private UserAdminDtos() {
    }

    /** BQL tạo tài khoản nội bộ (kế toán, kỹ thuật, BQL khác). */
    public record CreateUserRequest(
            @NotBlank @Size(max = 150) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            String phone,
            @NotNull Role role,
            @NotBlank @Pattern(regexp = AuthDtos.PASSWORD_REGEX, message = AuthDtos.PASSWORD_MESSAGE) String password) {
    }

    public record UpdateRoleRequest(@NotNull Role role) {
    }

    public record UpdateStatusRequest(@NotNull UserStatus status) {
    }

    public record UserSummary(UUID id, String email, String fullName, String phone, Role role,
                              UserStatus status, UUID tenantId, Instant lastLoginAt, Instant createdAt) {

        public static UserSummary from(UserAccount u) {
            return new UserSummary(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole(),
                    u.getStatus(), u.getTenantId(), u.getLastLoginAt(), u.getCreatedAt());
        }
    }
}
