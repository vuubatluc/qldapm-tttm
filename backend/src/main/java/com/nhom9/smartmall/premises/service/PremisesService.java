package com.nhom9.smartmall.premises.service;

import com.nhom9.smartmall.common.exception.BusinessException;
import com.nhom9.smartmall.common.response.PageResponse;
import com.nhom9.smartmall.premises.dto.PremisesDtos.FloorResponse;
import com.nhom9.smartmall.premises.dto.PremisesDtos.UnitResponse;
import com.nhom9.smartmall.premises.dto.PremisesDtos.ZoneResponse;
import com.nhom9.smartmall.premises.entity.Unit;
import com.nhom9.smartmall.premises.entity.UnitStatus;
import com.nhom9.smartmall.premises.entity.UnitStatusLog;
import com.nhom9.smartmall.premises.repository.FloorRepository;
import com.nhom9.smartmall.premises.repository.UnitRepository;
import com.nhom9.smartmall.premises.repository.UnitStatusLogRepository;
import com.nhom9.smartmall.premises.repository.ZoneRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PremisesService {

    private final FloorRepository floorRepository;
    private final ZoneRepository zoneRepository;
    private final UnitRepository unitRepository;
    private final UnitStatusLogRepository statusLogRepository;

    @Transactional(readOnly = true)
    public List<FloorResponse> listFloors() {
        Map<UUID, List<ZoneResponse>> zonesByFloor = zoneRepository.findAllActiveWithFloor().stream()
                .collect(Collectors.groupingBy(z -> z.getFloor().getId(),
                        Collectors.mapping(ZoneResponse::from, Collectors.toList())));
        return floorRepository.findByActiveTrueOrderByLevelNo().stream()
                .map(f -> FloorResponse.from(f, zonesByFloor.getOrDefault(f.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<UnitResponse> searchUnits(UUID floorId, UUID zoneId, UnitStatus status, String keyword,
                                                  Pageable pageable) {
        String kw = "%" + (keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        return PageResponse.of(unitRepository.search(floorId, zoneId, status, kw, pageable), UnitResponse::from);
    }

    @Transactional(readOnly = true)
    public UnitResponse getUnit(UUID unitId) {
        return unitRepository.findWithZoneById(unitId).map(UnitResponse::from)
                .orElseThrow(() -> BusinessException.notFound("gian hàng"));
    }

    /**
     * Đổi trạng thái gian hàng và ghi log. Phải được gọi bên trong transaction của nghiệp vụ gọi nó
     * (ví dụ kích hoạt hợp đồng) để log và trạng thái cùng commit.
     */
    @Transactional
    public void changeStatus(Unit unit, UnitStatus newStatus, UUID actorId, String reason) {
        UnitStatus old = unit.getStatus();
        if (old == newStatus) {
            return;
        }
        unit.setStatus(newStatus);
        statusLogRepository.save(new UnitStatusLog(unit.getId(), old, newStatus, actorId, reason));
    }
}
