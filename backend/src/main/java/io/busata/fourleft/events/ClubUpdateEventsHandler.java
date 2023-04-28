package io.busata.fourleft.events;


import io.busata.fourleft.api.messages.*;
import io.busata.fourleft.domain.configuration.repository.ClubConfigurationRepository;
import io.busata.fourleft.automated.service.ChampionshipCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubUpdateEventsHandler {
    private final RabbitTemplate rabbitMQ;
    private final ClubConfigurationRepository clubConfigurationRepository;
    private final ChampionshipCreator championshipCreator;

    @Value("${io.busata.fourleft.scheduling.automated:true}")
    private boolean createChampionships;

    @EventListener
    public void handleLeaderboardUpdate(LeaderboardUpdated updated) {
        rabbitMQ.convertAndSend(QueueNames.LEADERBOARD_UPDATE, updated);
    }

    @EventListener
    public void handleClubUpdate(ClubEventStarted clubEventStarted) {
        rabbitMQ.convertAndSend(QueueNames.CLUB_EVENT_STARTED, clubEventStarted);
    }

    @EventListener
    public void handleClubInactive(ClubInactive event) {
        if(!createChampionships) {
            return;
        }

        if(createClubChampionship(event.clubId())) {
            rabbitMQ.convertAndSend(QueueNames.CLUB_EVENT_STARTED, new ClubEventStarted(event.clubId()));
        }
    }

    @EventListener
    public void handleEventEnded(ClubEventEnded event) {
        rabbitMQ.convertAndSend(QueueNames.CLUB_EVENT_ENDED, event);

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
