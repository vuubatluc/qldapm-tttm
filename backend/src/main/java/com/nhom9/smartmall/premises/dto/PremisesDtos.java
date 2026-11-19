package com.nhom9.smartmall.premises.dto;

import com.nhom9.smartmall.premises.entity.Floor;
import com.nhom9.smartmall.premises.entity.Unit;
import com.nhom9.smartmall.premises.entity.UnitStatus;
import com.nhom9.smartmall.premises.entity.Zone;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class PremisesDtos {

    private PremisesDtos() {
    }

    public record ZoneResponse(UUID id, String zoneCode, String name) {
        public static ZoneResponse from(Zone z) {
            return new ZoneResponse(z.getId(), z.getZoneCode(), z.getName());
        }
    }

    public record FloorResponse(UUID id, String floorCode, String name, int levelNo, List<ZoneResponse> zones) {
        public static FloorResponse from(Floor f, List<ZoneResponse> zones) {
            return new FloorResponse(f.getId(), f.getFloorCode(), f.getName(), f.getLevelNo(), zones);
        }
    }

    public record UnitResponse(UUID id, String unitCode, String floorCode, String zoneCode, String zoneName,
                               BigDecimal areaM2, UnitStatus status, String plannedCategory, BigDecimal refRent) {
        public static UnitResponse from(Unit u) {
            Zone z = u.getZone();
            return new UnitResponse(u.getId(), u.getUnitCode(), z.getFloor().getFloorCode(), z.getZoneCode(),
                    z.getName(), u.getAreaM2(), u.getStatus(), u.getPlannedCategory(), u.getRefRent());
        }
    }
}
