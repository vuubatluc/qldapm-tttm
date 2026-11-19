package com.nhom9.smartmall.tenant.repository;

import com.nhom9.smartmall.tenant.entity.Tenant;
import com.nhom9.smartmall.tenant.entity.TenantIdType;
import com.nhom9.smartmall.tenant.entity.TenantStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    boolean existsByIdTypeAndIdNumber(TenantIdType idType, String idNumber);

    boolean existsByIdTypeAndIdNumberAndIdNot(TenantIdType idType, String idNumber, UUID id);

    @Query("""
            select t from Tenant t
            where (:status is null or t.status = :status)
              and (lower(t.brandName) like :keyword
                   or lower(t.legalName) like :keyword
                   or lower(t.tenantCode) like :keyword
                   or t.idNumber like :keyword)
            """)
    Page<Tenant> search(@Param("status") TenantStatus status, @Param("keyword") String keyword, Pageable pageable);

    @Query(value = "select nextval('tenant_code_seq')", nativeQuery = true)
    long nextCodeSequence();
}
