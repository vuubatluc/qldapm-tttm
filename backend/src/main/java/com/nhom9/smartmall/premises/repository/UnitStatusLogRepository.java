package com.nhom9.smartmall.premises.repository;

import com.nhom9.smartmall.premises.entity.UnitStatusLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitStatusLogRepository extends JpaRepository<UnitStatusLog, UUID> {
}
