package com.nhom9.smartmall.tenant.entity;

import com.nhom9.smartmall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Hồ sơ khách thuê (đối tác thuê gian hàng). */
@Getter
@Setter
@Entity
@Table(name = "tenant")
public class Tenant extends BaseEntity {

    /** Mã nghiệp vụ hiển thị, ví dụ KT-0012. */
    @Column(name = "tenant_code", nullable = false, unique = true, length = 30)
    private String tenantCode;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "brand_name", nullable = false, length = 200)
    private String brandName;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false, length = 20)
    private TenantIdType idType;

    /** Mã số thuế hoặc số CCCD đã chuẩn hóa (bỏ khoảng trắng). */
    @Column(name = "id_number", nullable = false, length = 20)
    private String idNumber;

    @Column(length = 150)
    private String representative;

    @Column(name = "contact_email", nullable = false, length = 254)
    private String contactEmail;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    /** Ngành hàng: F&B, Thời trang, Mỹ phẩm... */
    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status;
}
