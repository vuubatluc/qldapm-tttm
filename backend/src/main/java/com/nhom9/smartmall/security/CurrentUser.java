package com.nhom9.smartmall.security;

import com.nhom9.smartmall.auth.entity.Role;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Thông tin người dùng hiện tại lấy từ JWT. Dùng ở controller:
 * {@code @AuthenticationPrincipal Jwt jwt} → {@code CurrentUser.from(jwt)}.
 */
public record CurrentUser(UUID id, String name, Role role, UUID tenantId) {

    public static CurrentUser from(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(JwtTokenService.CLAIM_ROLES);
        String tenant = jwt.getClaimAsString(JwtTokenService.CLAIM_TENANT);
        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString(JwtTokenService.CLAIM_NAME),
                Role.valueOf(roles.getFirst()),
                tenant == null ? null : UUID.fromString(tenant));
    }

    public boolean is(Role r) {
        return role == r;
    }
}
