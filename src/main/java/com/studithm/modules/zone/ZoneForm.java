package com.studithm.modules.zone;

import com.studithm.modules.zone.Zone;
import lombok.Data;

@Data
public class ZoneForm {
    private String zoneName;

    public String getCityName() {
        return zoneName.substring(0, zoneName.indexOf("("));
    }

    public String getProvinceName() {
        return zoneName.substring(zoneName.indexOf("/") + 1);
    }

    public String getLocalNameOfCity() {
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));
    }

    public Zone getZone() {
        return Zone.builder()
                .city(getCityName())
                .province(getProvinceName())
                .localNameOfCity(getLocalNameOfCity())
                .build();
    }
}
