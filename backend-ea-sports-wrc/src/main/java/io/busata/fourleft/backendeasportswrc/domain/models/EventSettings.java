package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class EventSettings {
    private Long vehicleClassID;
    private String vehicleClass;
    private Long weatherSeasonID;
    private String weatherSeason;
    private Long locationID;
    private String location;
    private String duration;
}
