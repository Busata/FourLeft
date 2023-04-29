package io.busata.fourleft.application.dirtrally2;


import io.busata.fourleft.api.events.*;
import io.busata.fourleft.domain.aggregators.repository.ClubConfigurationRepository;
import io.busata.fourleft.application.dirtrally2.automated.service.ChampionshipCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubUpdateEventsHandler {
    private final ClubConfigurationRepository clubConfigurationRepository;
    private final ChampionshipCreator championshipCreator;

    private final ApplicationEventPublisher eventPublisher;

    @Value("${io.busata.fourleft.scheduling.automated:true}")
    private boolean createChampionships;


    @EventListener
    public void handleClubInactive(ClubInactive event) {
        if(!createChampionships) {
            return;
        }

        if(createClubChampionship(event.clubId())) {
            eventPublisher.publishEvent(new ClubEventStarted(event.clubId()));
        }
    }

    @EventListener
    public void handleEventEnded(ClubEventEnded event) {

        if(!createChampionships) {
            return;
        }

        createClubChampionship(event.clubId());
    }

    private boolean createClubChampionship(long clubId) {
       return clubConfigurationRepository.findAll().stream()
                .filter(clubConfiguration -> clubConfiguration.getClubId() == clubId)
                .findFirst()
                .map(clubConfiguration -> {
                    switch (clubConfiguration.getAutomatedGenerationType()) {
                        case DAILY -> championshipCreator.createDailyChampionship(clubConfiguration.getClubId());
                        case MONTHLY -> championshipCreator.createWeeklyChampionship(clubConfiguration.getClubId());
                    }
                    return true;
                }).orElse(false);
    }

}
