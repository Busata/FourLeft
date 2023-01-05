package io.busata.fourleft.events;


import io.busata.fourleft.api.messages.ClubOperation;
import io.busata.fourleft.api.messages.ClubUpdated;
import io.busata.fourleft.api.messages.QueueNames;
import io.busata.fourleft.domain.configuration.repository.ClubConfigurationRepository;
import io.busata.fourleft.endpoints.club.automated.service.ChampionshipCreator;
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

    @Value("${io.busata.fourleft.scheduling.automated}")
    private boolean createChampionships;

    @EventListener
    public void handle(ClubUpdated clubUpdated) {
        rabbitMQ.convertAndSend(QueueNames.CLUB_EVENT_QUEUE, clubUpdated);

        if(clubUpdated.operation() == ClubOperation.EVENT_ENDED && createChampionships) {

            clubConfigurationRepository.findAll().stream()
                    .filter(clubConfiguration -> clubConfiguration.getClubId() == clubUpdated.clubId())
                    .findFirst()
                    .ifPresent(clubConfiguration -> {
                        switch (clubConfiguration.getAutomatedGenerationType()) {
                            case DAILY -> championshipCreator.createDailyChampionship(clubConfiguration.getClubId());
                            case MONTHLY -> championshipCreator.createWeeklyChampionship(clubConfiguration.getClubId());
                        }

                        rabbitMQ.convertAndSend(QueueNames.CLUB_EVENT_QUEUE, new ClubUpdated(ClubOperation.EVENT_STARTED, clubUpdated.clubId()));
                    });
        }

    }

}
