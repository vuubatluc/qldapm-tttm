package com.nhom9.smartmall.premises.repository;

import com.nhom9.smartmall.premises.entity.Unit;
import com.nhom9.smartmall.premises.entity.UnitStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnitRepository extends JpaRepository<Unit, UUID> {

    @EntityGraph(attributePaths = {"zone", "zone.floor"})
    @Query("""
            select u from Unit u
            where (:floorId is null or u.zone.floor.id = :floorId)
              and (:zoneId is null or u.zone.id = :zoneId)
              and (:status is null or u.status = :status)
              and lower(u.unitCode) like :keyword
            """)
    Page<Unit> search(@Param("floorId") UUID floorId, @Param("zoneId") UUID zoneId,
                      @Param("status") UnitStatus status, @Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"zone", "zone.floor"})
    Optional<Unit> findWithZoneById(UUID id);

    /** Khóa dòng gian hàng (SELECT ... FOR UPDATE) trước khi kiểm tra trùng kỳ thuê. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Unit u where u.id = :id")
    Optional<Unit> findByIdForUpdate(@Param("id") UUID id);
}
