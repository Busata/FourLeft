package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipSettings {

    private String name;
    private Long format;
    private Long bonusPointsMode;
    private Long scoringSystem;
    private Long trackDegradation;
    private Boolean isHardcoreDamageEnabled;
    private Boolean isAssistsAllowed;
    private Boolean isTuningAllowe;


}
