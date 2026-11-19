package com.nhom9.smartmall.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Nhật ký đăng nhập (UC-TK01 bước 10). Chỉ ghi thêm, không sửa. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "login_audit_log")
public class LoginAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_reason", length = 50)
    private String failureReason;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public LoginAuditLog(UUID userId, String email, boolean success, String failureReason,
                         String ipAddress, String userAgent) {
        this.userId = userId;
        this.email = email;
        this.success = success;
        this.failureReason = failureReason;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent == null ? null : userAgent.substring(0, Math.min(500, userAgent.length()));
        this.createdAt = Instant.now();
    }
}
