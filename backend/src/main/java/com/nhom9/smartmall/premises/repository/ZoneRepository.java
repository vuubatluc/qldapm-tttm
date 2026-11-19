package com.nhom9.smartmall.premises.repository;

import com.nhom9.smartmall.premises.entity.Zone;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {

    @Query("select z from Zone z join fetch z.floor where z.active = true order by z.floor.levelNo, z.zoneCode")
    List<Zone> findAllActiveWithFloor();
}
