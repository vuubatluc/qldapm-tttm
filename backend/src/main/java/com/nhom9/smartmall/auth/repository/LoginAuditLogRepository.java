package com.nhom9.smartmall.auth.repository;

import com.nhom9.smartmall.auth.entity.LoginAuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditLogRepository extends JpaRepository<LoginAuditLog, UUID> {
}
