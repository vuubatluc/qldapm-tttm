package com.nhom9.smartmall.common.config;

import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.entity.UserAccount;
import com.nhom9.smartmall.auth.entity.UserStatus;
import com.nhom9.smartmall.auth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tạo tài khoản mẫu cho từng vai trò khi chạy profile dev.
 * Mật khẩu lấy từ cấu hình, không hard-code hash trong migration.
 */
@Slf4j
@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.dev-seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.dev-seed.password}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("admin@smartmall.local", "Ban Quản Lý", Role.ADMIN);
        seed("ketoan@smartmall.local", "Kế Toán", Role.ACCOUNTANT);
        seed("kythuat@smartmall.local", "Kỹ Thuật Viên", Role.TECHNICIAN);
    }

    private void seed(String email, String name, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        UserAccount u = new UserAccount();
        u.setEmail(email);
        u.setFullName(name);
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(u);
        log.info("Seeded dev account {} ({})", email, role);
    }
}
