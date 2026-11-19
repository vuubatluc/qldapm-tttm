package com.nhom9.smartmall.auth.service;

import com.nhom9.smartmall.auth.dto.AuthDtos.LoginRequest;
import com.nhom9.smartmall.auth.dto.AuthDtos.RegisterRequest;
import com.nhom9.smartmall.auth.dto.AuthDtos.TokenResponse;
import com.nhom9.smartmall.auth.dto.AuthDtos.UserInfo;
import com.nhom9.smartmall.auth.entity.LoginAuditLog;
import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.entity.UserAccount;
import com.nhom9.smartmall.auth.entity.UserStatus;
import com.nhom9.smartmall.auth.repository.LoginAuditLogRepository;
import com.nhom9.smartmall.auth.repository.UserAccountRepository;
import com.nhom9.smartmall.common.exception.BusinessException;
import com.nhom9.smartmall.common.exception.ErrorCode;
import com.nhom9.smartmall.security.JwtTokenService;
import com.nhom9.smartmall.security.JwtTokenService.IssuedToken;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Đăng nhập, đăng ký, làm mới token (UC-TK01). */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserAccountRepository userRepository;
    private final LoginAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    /**
     * Không đặt @Transactional ở đây: số lần sai và nhật ký phải được lưu
     * kể cả khi đăng nhập thất bại (ném exception).
     */
    public TokenResponse login(LoginRequest req, ClientInfo client) {
        String email = normalizeEmail(req.email());
        Instant now = Instant.now();
        UserAccount user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            audit(null, email, false, "USER_NOT_FOUND", client);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.isLocked(now)) {
            audit(user.getId(), email, false, "LOCKED", client);
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            registerFailedAttempt(user, now);
            audit(user.getId(), email, false, "BAD_PASSWORD", client);
            throw new BusinessException(user.isLocked(now) ? ErrorCode.ACCOUNT_LOCKED : ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() == UserStatus.PENDING) {
            audit(user.getId(), email, false, "PENDING", client);
            throw new BusinessException(ErrorCode.ACCOUNT_PENDING);
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            audit(user.getId(), email, false, "SUSPENDED", client);
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        userRepository.save(user);
        audit(user.getId(), email, true, null, client);
        return issueTokens(user);
    }

    /** Tự đăng ký: tạo tài khoản vai trò Khách thuê, trạng thái chờ BQL duyệt. */
    @Transactional
    public UserInfo register(RegisterRequest req) {
        String email = normalizeEmail(req.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(Role.TENANT);
        user.setStatus(UserStatus.PENDING);
        return UserInfo.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        UUID userId;
        try {
            userId = tokenService.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        UserAccount user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public UserInfo me(UUID userId) {
        return userRepository.findById(userId).map(UserInfo::from)
                .orElseThrow(() -> BusinessException.notFound("tài khoản"));
    }

    private void registerFailedAttempt(UserAccount user, Instant now) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(now.plus(LOCK_DURATION));
            user.setFailedAttempts(0);
            log.warn("Account {} locked after {} failed attempts", user.getId(), attempts);
        }
        userRepository.save(user);
    }

    private TokenResponse issueTokens(UserAccount user) {
        IssuedToken access = tokenService.issueAccessToken(user);
        IssuedToken refresh = tokenService.issueRefreshToken(user);
        return new TokenResponse(access.value(), access.expiresAt(), refresh.value(), refresh.expiresAt(),
                UserInfo.from(user));
    }

    /** login() không có transaction bao ngoài nên mỗi lần save() tự commit ngay. */
    private void audit(UUID userId, String email, boolean success, String reason, ClientInfo client) {
        auditLogRepository.save(new LoginAuditLog(userId, email, success, reason, client.ip(), client.userAgent()));
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record ClientInfo(String ip, String userAgent) {
    }
}
