package io.busata.fourleft.backendeasportswrc.infrastructure.rabbitmq;

import io.busata.fourleft.api.easportswrc.EASportsWRCQueueNames;
import io.busata.fourleft.api.easportswrc.events.LeaderboardUpdatedEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventRelayer {
    private final RabbitTemplate rabbitMQ;

    @EventListener(ApplicationReadyEvent.class)
    public void sendBootEvent() {
        rabbitMQ.convertAndSend(EASportsWRCQueueNames.EA_SPORTS_WRC_READY, true);

    }

    @EventListener
    public void handleLeaderboardUpdate(LeaderboardUpdatedEvent updated) {
        rabbitMQ.convertAndSend(EASportsWRCQueueNames.EA_SPORTS_WRC_LEADERBOARD_UPDATE, updated);
    }

}
