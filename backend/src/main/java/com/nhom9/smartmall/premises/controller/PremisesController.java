package com.nhom9.smartmall.premises.controller;

import com.nhom9.smartmall.common.response.PageResponse;
import com.nhom9.smartmall.premises.dto.PremisesDtos.FloorResponse;
import com.nhom9.smartmall.premises.dto.PremisesDtos.UnitResponse;
import com.nhom9.smartmall.premises.entity.UnitStatus;
import com.nhom9.smartmall.premises.service.PremisesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Tra cứu mặt bằng (RBAC: mọi vai trò nội bộ và khách thuê được xem). */
@Tag(name = "Premises", description = "Tầng, phân khu, gian hàng")
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class PremisesController {

    private final PremisesService premisesService;

    @Operation(summary = "Danh sách tầng kèm phân khu")
    @GetMapping("/floors")
    public List<FloorResponse> floors() {
        return premisesService.listFloors();
    }

    @Operation(summary = "Tìm gian hàng theo tầng, khu, trạng thái, mã")
    @GetMapping("/units")
    public PageResponse<UnitResponse> units(@RequestParam(required = false) UUID floorId,
                                            @RequestParam(required = false) UUID zoneId,
                                            @RequestParam(required = false) UnitStatus status,
                                            @RequestParam(required = false) String keyword,
                                            @PageableDefault(size = 50, sort = "unitCode", direction = Sort.Direction.ASC)
                                            Pageable pageable) {
        return premisesService.searchUnits(floorId, zoneId, status, keyword, pageable);
    }

    @Operation(summary = "Chi tiết gian hàng")
    @GetMapping("/units/{unitId}")
    public UnitResponse unit(@PathVariable UUID unitId) {
        return premisesService.getUnit(unitId);
    }
}
