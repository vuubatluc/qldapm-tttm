package com.nhom9.smartmall.premises.repository;

import com.nhom9.smartmall.premises.entity.Floor;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorRepository extends JpaRepository<Floor, UUID> {

    List<Floor> findByActiveTrueOrderByLevelNo();
}
