package io.busata.fourleftdiscord.listeners;

import discord4j.common.util.Snowflake;
import io.busata.fourleft.api.events.ClubEventEnded;
import io.busata.fourleft.api.events.ClubEventStarted;
import io.busata.fourleft.api.events.QueueNames;
import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.autoposting.automated_championships.AutoPosterAutomatedDailyClubService;
import io.busata.fourleftdiscord.autoposting.community_challenges.AutoPostCommunityEventResultsService;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name = "autoposting", havingValue = "true", matchIfMissing = true)
public class AutoPostListener {
    private final AutoPostCommunityEventResultsService autoPostCommunityEventResultsService;
    private final AutoPosterAutomatedDailyClubService autoPosterAutomatedDailyClubService;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;

    @RabbitListener(queues = QueueNames.COMMUNITY_UPDATED)
    public void updateChallenges(String message) {
        try {
            log.info("Community update message: {}", message);
            autoPostCommunityEventResultsService.update();
        } catch (Exception ex) {
            log.error("Error updating community challenges", ex);
        }
    }

    @RabbitListener(queues = QueueNames.CLUB_EVENT_ENDED)
    public void postPreviousEventResults(ClubEventEnded event) {
        try {
            log.info("Received club updated event: {}, event ended.", event.clubId());

            discordChannelConfigurationService.findConfigurationByClubId(event.clubId())
                    .forEach(configuration -> {
                        final var channelId = Snowflake.of(configuration.channelId());
                        autoPosterAutomatedDailyClubService.postResults(channelId);
                        autoPosterAutomatedDailyClubService.postChampionship(channelId);
                    });
        } catch (Exception ex) {
            log.error("Error posting results for club: {}", event.clubId(), ex);
        }
    }

    @RabbitListener(queues = QueueNames.CLUB_EVENT_STARTED)
    public void postNewEventInfo(ClubEventStarted event) {
        try {
            log.info("Received club updated event: {}, event started.", event.clubId());

            discordChannelConfigurationService.findConfigurationByClubId(event.clubId()).stream()
                    .map(DiscordChannelConfigurationTo::channelId)
                    .map(Snowflake::of)
                    .forEach(autoPosterAutomatedDailyClubService::postNewStage);
        } catch (Exception ex) {
            log.error("Error posting results for club: {}", event.clubId(), ex);
        }
    }
}
