package com.nhom9.smartmall.user.service;

import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.entity.UserAccount;
import com.nhom9.smartmall.auth.entity.UserStatus;
import com.nhom9.smartmall.auth.repository.UserAccountRepository;
import com.nhom9.smartmall.auth.service.AuthService;
import com.nhom9.smartmall.common.exception.BusinessException;
import com.nhom9.smartmall.common.exception.ErrorCode;
import com.nhom9.smartmall.common.response.PageResponse;
import com.nhom9.smartmall.user.dto.UserAdminDtos.CreateUserRequest;
import com.nhom9.smartmall.user.dto.UserAdminDtos.UserSummary;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Quản trị tài khoản & phân quyền (FR-TK-02). Chỉ BQL được gọi. */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UserSummary> search(Role role, UserStatus status, boolean unlinkedOnly, String keyword,
                                            Pageable pageable) {
        String kw = "%" + (keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        return PageResponse.of(userRepository.search(role, status, unlinkedOnly, kw, pageable), UserSummary::from);
    }

    /**
     * Gắn tài khoản khách thuê tự đăng ký vào hồ sơ khách thuê (tenant đã được kiểm tra tồn tại ở TenantService).
     * Người dùng cần đăng nhập lại để token mang tenantId mới.
     */
    @Transactional
    public UserSummary linkTenantAccount(UUID userId, UUID tenantId) {
        UserAccount u = load(userId);
        if (u.getRole() != Role.TENANT) {
            throw new BusinessException(ErrorCode.USER_NOT_TENANT_ROLE);
        }
        if (u.getTenantId() != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_LINKED);
        }
        if (userRepository.existsByTenantId(tenantId)) {
            throw new BusinessException(ErrorCode.TENANT_ACCOUNT_EXISTS);
        }
        u.setTenantId(tenantId);
        return UserSummary.from(u);
    }

    @Transactional
    public void unlinkTenantAccount(UUID tenantId) {
        UserAccount u = userRepository.findByTenantId(tenantId)
                .orElseThrow(() -> BusinessException.notFound("tài khoản liên kết với khách thuê"));
        u.setTenantId(null);
    }

    @Transactional(readOnly = true)
    public String findLinkedEmail(UUID tenantId) {
        return userRepository.findByTenantId(tenantId).map(UserAccount::getEmail).orElse(null);
    }

    @Transactional
    public UserSummary create(CreateUserRequest req) {
        String email = AuthService.normalizeEmail(req.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        UserAccount u = new UserAccount();
        u.setEmail(email);
        u.setFullName(req.fullName().trim());
        u.setPhone(req.phone());
        u.setRole(req.role());
        u.setStatus(UserStatus.ACTIVE);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        return UserSummary.from(userRepository.save(u));
    }

    @Transactional
    public UserSummary changeRole(UUID userId, Role role, UUID actorId) {
        UserAccount u = load(userId);
        guardSelf(userId, actorId);
        u.setRole(role);
        return UserSummary.from(u);
    }

    @Transactional
    public UserSummary changeStatus(UUID userId, UserStatus status, UUID actorId) {
        UserAccount u = load(userId);
        guardSelf(userId, actorId);
        u.setStatus(status);
        if (status == UserStatus.ACTIVE) {
            u.setFailedAttempts(0);
            u.setLockedUntil(null);
        }
        return UserSummary.from(u);
    }

    /**
     * Cấp tài khoản Cổng thông tin cho khách thuê (UC-KT-LEASING bước 11).
     * Trả về mật khẩu tạm để BQL chuyển cho khách thuê; mật khẩu không được lưu dạng rõ.
     */
    @Transactional
    public TenantAccountResult createTenantAccount(UUID tenantId, String email, String fullName, String phone) {
        String normalized = AuthService.normalizeEmail(email);
        if (userRepository.existsByTenantId(tenantId)) {
            throw new BusinessException(ErrorCode.TENANT_ACCOUNT_EXISTS);
        }
        if (userRepository.existsByEmail(normalized)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        String tempPassword = generateTempPassword();
        UserAccount u = new UserAccount();
        u.setEmail(normalized);
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setRole(Role.TENANT);
        u.setStatus(UserStatus.ACTIVE);
        u.setTenantId(tenantId);
        u.setPasswordHash(passwordEncoder.encode(tempPassword));
        userRepository.save(u);
        return new TenantAccountResult(u.getId(), normalized, tempPassword);
    }

    public record TenantAccountResult(UUID userId, String email, String temporaryPassword) {
    }

    private static String generateTempPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        // Bảo đảm thỏa chính sách mật khẩu: có hoa, thường, số, ký tự đặc biệt.
        return sb.append("Aa1@").toString();
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private UserAccount load(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> BusinessException.notFound("tài khoản"));
    }

    /** Không cho BQL tự hạ quyền / khóa chính mình (tránh mất tài khoản quản trị cuối cùng). */
    private void guardSelf(UUID userId, UUID actorId) {
        if (userId.equals(actorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Không thể tự thay đổi vai trò/trạng thái của chính mình");
        }
    }
}
