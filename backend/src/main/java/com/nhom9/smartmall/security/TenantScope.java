package com.nhom9.smartmall.security;

import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.repository.UserAccountRepository;
import com.nhom9.smartmall.common.exception.BusinessException;
import com.nhom9.smartmall.common.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xác định hồ sơ khách thuê mà tài khoản TENANT đang đăng nhập được phép truy cập.
 * Đọc từ CSDL (không tin claim trong token) để việc liên kết / hủy liên kết có hiệu lực ngay.
 */
@Component
@RequiredArgsConstructor
public class TenantScope {

    private final UserAccountRepository userRepository;

    @Transactional(readOnly = true)
    public UUID requireOwnTenantId(Jwt jwt) {
        UUID userId = CurrentUser.from(jwt).id();
        return userRepository.findById(userId)
                .map(u -> u.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "Tài khoản chưa được liên kết với hồ sơ khách thuê. Vui lòng liên hệ Ban quản lý."));
    }

    /** Ném 403 nếu người dùng là TENANT mà tenantId không phải của mình. Vai trò khác bỏ qua. */
    public void checkAccess(Jwt jwt, UUID tenantId) {
        if (CurrentUser.from(jwt).is(Role.TENANT) && !tenantId.equals(requireOwnTenantIdOrNull(jwt))) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private UUID requireOwnTenantIdOrNull(Jwt jwt) {
        return userRepository.findById(CurrentUser.from(jwt).id()).map(u -> u.getTenantId()).orElse(null);
    }
}
