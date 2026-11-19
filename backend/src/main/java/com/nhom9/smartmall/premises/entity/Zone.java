package com.nhom9.smartmall.premises.entity;

import com.nhom9.smartmall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "zone")
public class Zone extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @Column(name = "zone_code", nullable = false, length = 20)
    private String zoneCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
