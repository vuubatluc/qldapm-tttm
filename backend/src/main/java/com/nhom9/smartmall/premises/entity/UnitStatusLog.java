package com.nhom9.smartmall.premises.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Ghi mỗi lần đổi trạng thái gian hàng, trong cùng giao dịch với UNIT. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "unit_status_log")
public class UnitStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false, length = 20)
    private UnitStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private UnitStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    public UnitStatusLog(UUID unitId, UnitStatus oldStatus, UnitStatus newStatus, UUID actorId, String reason) {
        this.unitId = unitId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.actorId = actorId;
        this.reason = reason;
        this.changedAt = Instant.now();
    }
}
