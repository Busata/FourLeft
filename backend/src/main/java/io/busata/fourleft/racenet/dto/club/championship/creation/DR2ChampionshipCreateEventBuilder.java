package io.busata.fourleft.racenet.dto.club.championship.creation;


import io.busata.fourleft.domain.options.models.CountryOption;
import io.busata.fourleft.domain.options.models.VehicleClass;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Accessors(fluent = true)
public class DR2ChampionshipCreateEventBuilder {

    String disciplineId = "eRally";

    @Setter
    long durationDays = 0;

    @Setter
    long durationHours = 0;

    @Setter
    long durationMins = 0;

    @Setter
    CountryOption country;

    @Setter
    VehicleClass vehicle;

    List<DR2ChampionshipCreateStage> stages = new ArrayList<>();


    public DR2ChampionshipCreateEventBuilder withStages(DR2ChampionshipCreateStageBuilder... stages) {
        validateStages(stages);
        this.stages = Arrays.stream(stages).map(DR2ChampionshipCreateStageBuilder::build).collect(Collectors.toList());
        return this;
    }

    private void validateStages(DR2ChampionshipCreateStageBuilder[] stages) {
        if(stages.length > 12) {
            throw new RuntimeException("Too many stages, max 12");
        }

        for (DR2ChampionshipCreateStageBuilder stage : stages) {
            if(stage.route.country() != country) {
                throw new RuntimeException("Cannot add this stage, it is not part of the event's country");
            }
        }
    }


    public static DR2ChampionshipCreateEventBuilder event() {
        return new DR2ChampionshipCreateEventBuilder();
    }

    public DR2ChampionShipCreateEvent build() {
        return new DR2ChampionShipCreateEvent(disciplineId, durationDays, durationHours, durationMins, country.getId(), vehicle.id(), stages);
    }
}
