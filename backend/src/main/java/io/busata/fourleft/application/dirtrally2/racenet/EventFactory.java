package io.busata.fourleft.application.dirtrally2.racenet;

import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubResultChampionshipEvent;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubChampionships;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Event;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Stage;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Factory
@RequiredArgsConstructor
public class EventFactory {

    private final StageFactory stageFactory;

    public Event create(DR2ClubResultChampionshipEvent resultEvent) {
        Event event =


                new Event();



        event.setChallengeId(resultEvent.challengeId());
        event.setName(resultEvent.name());
        event.setReferenceId(resultEvent.id());

        List<Stage> stages = resultEvent.stages().stream().map(stageFactory::createStage).peek(stage -> stage.setEvent(event)).collect(Collectors.toList());

        event.updateStages(stages);

        return event;
    }


    public List<Event> enrichEvents(DR2ClubChampionships details, List<Event> events) {
        return events.stream().flatMap(event ->
                details.events().stream().filter(eventDetails -> eventDetails.id().equals(event.getChallengeId())).findFirst()
                        .map(eventDetails -> {
                            event.setEventStatus(eventDetails.eventStatus());

                            ZonedDateTime start = ZonedDateTime.parse(eventDetails.entryWindow().start());
                            ZonedDateTime end = ZonedDateTime.parse(eventDetails.entryWindow().end());

                            event.setStartTime(start);
                            event.setEndTime(end);

                            event.setVehicleClass(eventDetails.vehicleClass());
                            event.setCountry(eventDetails.countryId());
                            event.setFirstStageCondition(eventDetails.firstStageConditions());
                            return event;
                        }).stream()
        ).collect(Collectors.toList());
    }
}
