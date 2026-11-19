package com.nhom9.smartmall.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Cấu hình JWT. {@code secret} phải lấy từ biến môi trường ở môi trường thật, tối thiểu 32 ký tự (HS256).
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret,
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl) {

    public JwtProperties {
        issuer = issuer == null ? "smartmall" : issuer;
        accessTokenTtl = accessTokenTtl == null ? Duration.ofHours(2) : accessTokenTtl;
        refreshTokenTtl = refreshTokenTtl == null ? Duration.ofDays(7) : refreshTokenTtl;
    }
}
