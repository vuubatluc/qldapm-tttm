package com.nhom9.smartmall.auth.dto;

import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.entity.UserAccount;
import com.nhom9.smartmall.auth.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/** DTO cho module xác thực. Gom chung một file vì đều nhỏ và chỉ dùng trong module auth. */
public final class AuthDtos {

    /** Mật khẩu ≥ 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt (UC-TK01, 3a). */
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";
    public static final String PASSWORD_MESSAGE =
            "Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt";

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 150) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
            @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE) String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record TokenResponse(
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            UserInfo user) {
    }

    public record UserInfo(UUID id, String email, String fullName, String phone,
                           Role role, UserStatus status, UUID tenantId) {

        public static UserInfo from(UserAccount u) {
            return new UserInfo(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(),
                    u.getRole(), u.getStatus(), u.getTenantId());
        }
    }
}
