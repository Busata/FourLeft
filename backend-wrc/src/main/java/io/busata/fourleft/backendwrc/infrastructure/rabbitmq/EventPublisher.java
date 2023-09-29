package io.busata.fourleft.backendwrc.infrastructure.rabbitmq;

import io.busata.fourleft.api.events.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static io.busata.fourleft.api.events.QueueNames.MESSAGES_QUEUE;

@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final RabbitTemplate rabbitMQ;

    @EventListener
    public void handleLeaderboardUpdate(LeaderboardUpdated updated) {
        rabbitMQ.convertAndSend(QueueNames.LEADERBOARD_UPDATE, updated);
    }

    @EventListener
    public void handleClubUpdate(ClubEventStarted clubEventStarted) {
        rabbitMQ.convertAndSend(QueueNames.CLUB_EVENT_STARTED, clubEventStarted);
    }
    @EventListener
    public void handleCommunityUpdate(CommunityChallengeUpdateEvent clubEventStarted) {
        rabbitMQ.convertAndSend(QueueNames.COMMUNITY_UPDATED, "OK");
    }

    @EventListener
    public void handleEventEnded(ClubEventEnded event) {
        rabbitMQ.convertAndSend(QueueNames.CLUB_EVENT_ENDED, event);
    }

    @EventListener
    public void handleMessageEvent(MessageEvent event) {
        rabbitMQ.convertAndSend(MESSAGES_QUEUE, event);
    }
    @EventListener
    public void handleWRCTickerEvent(FIATickerUpdateEvent event) {
        rabbitMQ.convertAndSend(QueueNames.TICKER_ENTRIES_UPDATE, event);
    }
}
