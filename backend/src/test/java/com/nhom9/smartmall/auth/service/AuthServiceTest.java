package com.nhom9.smartmall.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nhom9.smartmall.auth.dto.AuthDtos.LoginRequest;
import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.entity.UserAccount;
import com.nhom9.smartmall.auth.entity.UserStatus;
import com.nhom9.smartmall.auth.repository.LoginAuditLogRepository;
import com.nhom9.smartmall.auth.repository.UserAccountRepository;
import com.nhom9.smartmall.auth.service.AuthService.ClientInfo;
import com.nhom9.smartmall.common.exception.BusinessException;
import com.nhom9.smartmall.common.exception.ErrorCode;
import com.nhom9.smartmall.security.JwtTokenService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final ClientInfo CLIENT = new ClientInfo("127.0.0.1", "JUnit");
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    @Mock
    private UserAccountRepository userRepository;
    @Mock
    private LoginAuditLogRepository auditLogRepository;
    @Mock
    private JwtTokenService tokenService;

    private AuthService authService;
    private UserAccount user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, auditLogRepository, ENCODER, tokenService);
        user = new UserAccount();
        user.setEmail("ketoan@smartmall.local");
        user.setFullName("Kế Toán");
        user.setRole(Role.ACCOUNTANT);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(ENCODER.encode("Correct@123"));
        when(userRepository.findByEmail("ketoan@smartmall.local")).thenReturn(Optional.of(user));
    }

    @Test
    void wrongPassword_returnsGenericError_andCountsAttempt() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("KeToan@smartmall.local ", "bad"), CLIENT))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        assertThat(user.getFailedAttempts()).isEqualTo(1);
    }

    @Test
    void fifthWrongPassword_locksAccountFor15Minutes() {
        for (int i = 0; i < AuthService.MAX_FAILED_ATTEMPTS - 1; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "bad"), CLIENT))
                    .isInstanceOf(BusinessException.class);
        }
        assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "bad"), CLIENT))
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);
        assertThat(user.isLocked(Instant.now())).isTrue();

        // Kể cả mật khẩu đúng cũng bị từ chối khi đang khóa
        assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "Correct@123"), CLIENT))
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    void pendingAccount_cannotLogin() {
        user.setStatus(UserStatus.PENDING);
        assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "Correct@123"), CLIENT))
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_PENDING);
    }

    @Test
    void successfulLogin_resetsCounter_andIssuesTokens() {
        user.setFailedAttempts(3);
        when(tokenService.issueAccessToken(any())).thenReturn(new JwtTokenService.IssuedToken("a", Instant.now()));
        when(tokenService.issueRefreshToken(any())).thenReturn(new JwtTokenService.IssuedToken("r", Instant.now()));

        var res = authService.login(new LoginRequest(user.getEmail(), "Correct@123"), CLIENT);

        assertThat(res.accessToken()).isEqualTo("a");
        assertThat(res.user().role()).isEqualTo(Role.ACCOUNTANT);
        assertThat(user.getFailedAttempts()).isZero();
        assertThat(user.getLastLoginAt()).isNotNull();
    }
}
