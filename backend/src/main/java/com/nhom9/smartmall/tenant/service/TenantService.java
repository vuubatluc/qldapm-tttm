package com.nhom9.smartmall.tenant.service;

import com.nhom9.smartmall.common.exception.BusinessException;
import com.nhom9.smartmall.common.exception.ErrorCode;
import com.nhom9.smartmall.tenant.dto.TenantDtos.PortalAccount;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantCreatedResponse;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantRequest;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantResponse;
import com.nhom9.smartmall.tenant.dto.TenantDtos.TenantSelfUpdateRequest;
import com.nhom9.smartmall.tenant.entity.Tenant;
import com.nhom9.smartmall.tenant.entity.TenantIdType;
import com.nhom9.smartmall.tenant.entity.TenantStatus;
import com.nhom9.smartmall.tenant.repository.TenantRepository;
import com.nhom9.smartmall.user.service.UserAdminService;
import com.nhom9.smartmall.user.service.UserAdminService.TenantAccountResult;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ghi hồ sơ khách thuê (UC-KT-LEASING). Truy vấn tổng hợp nằm ở {@link TenantQueryService}. */
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserAdminService userAdminService;

    @Transactional
    public TenantCreatedResponse create(TenantRequest req) {
        String idNumber = normalizeIdNumber(req.idType(), req.idNumber());
        if (tenantRepository.existsByIdTypeAndIdNumber(req.idType(), idNumber)) {
            throw new BusinessException(ErrorCode.TENANT_ID_DUPLICATED);
        }
        Tenant t = new Tenant();
        t.setTenantCode(String.format("KT-%04d", tenantRepository.nextCodeSequence()));
        t.setStatus(TenantStatus.ACTIVE);
        apply(t, req, idNumber);
        tenantRepository.save(t);

        PortalAccount account = null;
        if (req.createPortalAccount()) {
            TenantAccountResult r = userAdminService.createTenantAccount(
                    t.getId(), t.getContactEmail(), t.getBrandName(), t.getPhone());
            account = new PortalAccount(r.email(), r.temporaryPassword());
        }
        return new TenantCreatedResponse(TenantResponse.from(t), account);
    }

    @Transactional
    public TenantResponse update(UUID id, TenantRequest req) {
        Tenant t = require(id);
        String idNumber = normalizeIdNumber(req.idType(), req.idNumber());
        if (tenantRepository.existsByIdTypeAndIdNumberAndIdNot(req.idType(), idNumber, id)) {
            throw new BusinessException(ErrorCode.TENANT_ID_DUPLICATED);
        }
        apply(t, req, idNumber);
        return TenantResponse.from(t);
    }

    @Transactional
    public TenantResponse selfUpdate(UUID id, TenantSelfUpdateRequest req) {
        Tenant t = require(id);
        t.setRepresentative(req.representative());
        t.setPhone(req.phone());
        t.setAddress(req.address());
        return TenantResponse.from(t);
    }

    @Transactional
    public TenantResponse changeStatus(UUID id, TenantStatus status) {
        Tenant t = require(id);
        t.setStatus(status);
        return TenantResponse.from(t);
    }

    @Transactional
    public PortalAccount createPortalAccount(UUID id) {
        Tenant t = require(id);
        TenantAccountResult r = userAdminService.createTenantAccount(
                t.getId(), t.getContactEmail(), t.getBrandName(), t.getPhone());
        return new PortalAccount(r.email(), r.temporaryPassword());
    }

    @Transactional
    public PortalAccount linkExistingAccount(UUID id, UUID userId) {
        Tenant t = require(id);
        var linked = userAdminService.linkTenantAccount(userId, t.getId());
        return new PortalAccount(linked.email(), null);
    }

    @Transactional
    public void unlinkAccount(UUID id) {
        userAdminService.unlinkTenantAccount(require(id).getId());
    }

    @Transactional(readOnly = true)
    public Tenant require(UUID id) {
        return tenantRepository.findById(id).orElseThrow(() -> BusinessException.notFound("khách thuê"));
    }

    private static void apply(Tenant t, TenantRequest req, String idNumber) {
        t.setLegalName(req.legalName().trim());
        t.setBrandName(req.brandName().trim());
        t.setIdType(req.idType());
        t.setIdNumber(idNumber);
        t.setRepresentative(req.representative());
        t.setContactEmail(req.contactEmail().trim().toLowerCase(Locale.ROOT));
        t.setPhone(req.phone());
        t.setAddress(req.address());
        t.setCategory(req.category());
    }

    static String normalizeIdNumber(TenantIdType type, String raw) {
        String v = raw.replaceAll("[\\s-]", "");
        boolean ok = switch (type) {
            case TAX_CODE -> v.matches("\\d{10}|\\d{13}");
            case CITIZEN_ID -> v.matches("\\d{12}");
        };
        if (!ok) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    type == TenantIdType.TAX_CODE ? "Mã số thuế phải gồm 10 hoặc 13 số" : "Số CCCD phải gồm 12 số");
        }
        return v;
    }
}
