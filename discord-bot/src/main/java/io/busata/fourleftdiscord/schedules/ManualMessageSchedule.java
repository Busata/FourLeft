package io.busata.fourleftdiscord.schedules;

import discord4j.common.util.Snowflake;
import io.busata.fourleft.api.models.messages.MessageCreateEvent;
import io.busata.fourleft.api.models.messages.MessageDeleteEvent;
import io.busata.fourleft.api.models.messages.MessageUpdateEvent;
import io.busata.fourleftdiscord.client.FourLeftClient;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import io.busata.fourleft.domain.discord.models.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="autoposting", havingValue="true", matchIfMissing = true)
public class ManualMessageSchedule {
    private final FourLeftClient api;
    private final DiscordMessageGateway facade;

    @Scheduled(cron = "*/5 * * * * *", zone="Europe/Brussels")
    public void checkMessageEvents() {
        api.getEvents().forEach(event -> {
            try {
                if (MessageCreateEvent.class.isAssignableFrom(event.getClass())) {
                    MessageCreateEvent createEvent = (MessageCreateEvent) event;
                    facade.postMessage(Snowflake.of(createEvent.channelId()), createEvent.content(), MessageType.RESULTS_POST);
                } else if (MessageDeleteEvent.class.isAssignableFrom(event.getClass())) {
                    MessageDeleteEvent deleteEvent = (MessageDeleteEvent) event;
                    facade.removeMessage(Snowflake.of(deleteEvent.channelId()), Snowflake.of(deleteEvent.messageId()));
                } else if (MessageUpdateEvent.class.isAssignableFrom(event.getClass())) {
                    MessageUpdateEvent updateEvent = (MessageUpdateEvent) event;
                    facade.editMessage(Snowflake.of(updateEvent.channelId()), Snowflake.of(updateEvent.messageId()), updateEvent.content());
                }
            }
            finally {
                api.completeEvent(event.getId());
            }
        });
    }
}
