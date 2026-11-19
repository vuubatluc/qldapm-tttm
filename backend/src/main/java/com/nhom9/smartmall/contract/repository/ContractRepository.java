package com.nhom9.smartmall.contract.repository;

import com.nhom9.smartmall.contract.entity.Contract;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    boolean existsByContractNo(String contractNo);

    @EntityGraph(attributePaths = {"tenant", "units", "units.unit"})
    Optional<Contract> findDetailById(UUID id);

    @EntityGraph(attributePaths = {"tenant", "units", "units.unit"})
    List<Contract> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
