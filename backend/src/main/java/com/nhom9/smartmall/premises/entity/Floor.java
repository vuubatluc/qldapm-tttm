package com.nhom9.smartmall.premises.entity;

import com.nhom9.smartmall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "floor")
public class Floor extends BaseEntity {

    @Column(name = "floor_code", nullable = false, unique = true, length = 20)
    private String floorCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "level_no", nullable = false)
    private int levelNo;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
