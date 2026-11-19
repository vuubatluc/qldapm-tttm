package com.nhom9.smartmall.auth.repository;

import com.nhom9.smartmall.auth.entity.Role;
import com.nhom9.smartmall.auth.entity.UserAccount;
import com.nhom9.smartmall.auth.entity.UserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByTenantId(UUID tenantId);

    Optional<UserAccount> findByTenantId(UUID tenantId);

    @Query("""
            select u from UserAccount u
            where (:role is null or u.role = :role)
              and (:status is null or u.status = :status)
              and (:unlinkedOnly = false or u.tenantId is null)
              and (lower(u.email) like :keyword or lower(u.fullName) like :keyword)
            """)
    Page<UserAccount> search(@Param("role") Role role, @Param("status") UserStatus status,
                             @Param("unlinkedOnly") boolean unlinkedOnly,
                             @Param("keyword") String keyword, Pageable pageable);
}
