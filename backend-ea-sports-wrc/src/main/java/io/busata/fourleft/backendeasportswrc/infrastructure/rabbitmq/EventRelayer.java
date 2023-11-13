package io.busata.fourleft.backendeasportswrc.infrastructure.rabbitmq;

import io.busata.fourleft.api.easportswrc.EASportsWRCQueueNames;
import io.busata.fourleft.api.easportswrc.events.ChannelUpdatedEvent;
import io.busata.fourleft.api.easportswrc.events.LeaderboardUpdatedEvent;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventRelayer {
    private final RabbitTemplate rabbitMQ;
    private final DiscordClubConfigurationService clubConfigurationService;

    @EventListener(ApplicationReadyEvent.class)
    public void sendBootEvent() {
        rabbitMQ.convertAndSend(EASportsWRCQueueNames.EA_SPORTS_WRC_READY, true);

    }

    @EventListener
    public void handleLeaderboardUpdate(LeaderboardUpdatedEvent updated) {
        clubConfigurationService.findByClubId(updated.clubId()).forEach(configuration -> {
            rabbitMQ.convertAndSend(EASportsWRCQueueNames.EA_SPORTS_WRC_CHANNEL_UPDATE, new ChannelUpdatedEvent(configuration.getChannelId()));
        });
    }

}
