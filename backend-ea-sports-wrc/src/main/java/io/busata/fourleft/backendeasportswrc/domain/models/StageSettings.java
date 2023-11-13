package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StageSettings {
    private Long routeID;
    private String route;
    private Long weatherAndSurfaceID;
    private String weatherAndSurface;
    private Long timeOfDayID;
    private String timeOfDay;
    private Long serviceAreaID;
    private String serviceAre;
}
