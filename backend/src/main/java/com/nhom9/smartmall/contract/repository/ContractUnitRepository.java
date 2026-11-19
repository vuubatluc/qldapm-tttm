package com.nhom9.smartmall.contract.repository;

import com.nhom9.smartmall.contract.entity.ContractStatus;
import com.nhom9.smartmall.contract.entity.ContractUnit;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractUnitRepository extends JpaRepository<ContractUnit, UUID> {

    /** Giao nhau khi: bắt đầu mới &lt; kết thúc cũ VÀ bắt đầu cũ &lt; kết thúc mới. Hai kỳ liền kề hợp lệ. */
    @Query("""
            select count(cu) > 0 from ContractUnit cu
            where cu.unit.id = :unitId
              and cu.contract.id <> :contractId
              and cu.contract.status in :statuses
              and :from < cu.leaseToExcl
              and cu.leaseFrom < :toExcl
            """)
    boolean existsOverlap(@Param("unitId") UUID unitId, @Param("contractId") UUID contractId,
                          @Param("from") LocalDate from, @Param("toExcl") LocalDate toExcl,
                          @Param("statuses") Collection<ContractStatus> statuses);

    @Query("""
            select cu from ContractUnit cu
              join fetch cu.unit
              join fetch cu.contract c
            where c.tenant.id in :tenantIds and c.status in :statuses
            """)
    List<ContractUnit> findLeasesOfTenants(@Param("tenantIds") Collection<UUID> tenantIds,
                                           @Param("statuses") Collection<ContractStatus> statuses);
}
