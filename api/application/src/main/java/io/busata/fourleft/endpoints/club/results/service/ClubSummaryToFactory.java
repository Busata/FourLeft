package io.busata.fourleft.endpoints.club.results.service;

import io.busata.fourleft.api.models.ChampionshipEventEntryTo;
import io.busata.fourleft.api.models.ChampionshipEventSummaryTo;
import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

import static java.util.List.of;

@Component
@RequiredArgsConstructor
public class ClubSummaryToFactory {

    public ChampionshipEventSummaryTo createEventSummary(Championship championship) {
        return new ChampionshipEventSummaryTo(
                championship.getName(),
                championship.isHardcoreDamage(),
                championship.isAllowAssists(),
                championship.isUnexpectedMoments(),
                championship.isForceCockpitCamera(),
                championship.getEvents().stream().sorted(Comparator.comparing(Event::getReferenceId)).map(event -> {
                    var country = event.getCountry();
                    var stage = event.getStages().get(event.getStages().size() - 1);
                    var firstStageCondition = event.getFirstStageCondition();
                    var vehicleClass = event.getVehicleClass();
                    var isCurrent = event.isCurrent();
                    var isFinished = event.isPrevious();

                    return new ChampionshipEventEntryTo(country,
                            event.getName(),
                            stage.getName(),
                            event.getChallengeId(),
                            event.getReferenceId(),
                            stage.getReferenceId(),
                            firstStageCondition,
                            vehicleClass,
                            isCurrent,
                            isFinished);
                }).collect(Collectors.toList())
        );
    }
}
