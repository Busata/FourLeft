package io.busata.fourleftdiscord.listeners;

import discord4j.common.util.Snowflake;
import io.busata.fourleft.api.messages.ClubOperation;
import io.busata.fourleft.api.messages.ClubUpdated;
import io.busata.fourleft.api.messages.QueueNames;
import io.busata.fourleft.api.models.configuration.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.autoposting.community_challenges.AutoPostCommunityEventResultsService;
import io.busata.fourleftdiscord.autoposting.automated_championships.AutoPosterAutomatedDailyClubService;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="autoposting", havingValue="true", matchIfMissing = true)
public class AutoPostListener {
    private final AutoPostCommunityEventResultsService autoPostCommunityEventResultsService;
    private final AutoPosterAutomatedDailyClubService autoPosterAutomatedDailyClubService;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;

    @RabbitListener(queues = QueueNames.COMMUNITY_UPDATED)
    public void updateChallenges(String message) {
        log.info("Community update message: {}", message);
        autoPostCommunityEventResultsService.update();
    }

    @RabbitListener(queues = QueueNames.CLUB_EVENT_QUEUE)
    public void postPreviousEventResults(ClubUpdated event) {
        if(event.operation() == ClubOperation.EVENT_ENDED) {
            log.info("Received club updated event: {}, event ended.", event.clubId());
            discordChannelConfigurationService.findConfigurationByClubId(event.clubId())
                .forEach(configuration -> {
                    final var channelId = Snowflake.of(configuration.channelId());
                    autoPosterAutomatedDailyClubService.postResults(channelId);
                    autoPosterAutomatedDailyClubService.postChampionship(channelId);
                });
        }

        if(event.operation() == ClubOperation.EVENT_STARTED) {
            log.info("Received club updated event: {}, event started.", event.clubId());
            discordChannelConfigurationService.findConfigurationByClubId(event.clubId()).stream()
                    .map(DiscordChannelConfigurationTo::channelId)
                    .map(Snowflake::of)
                    .forEach(autoPosterAutomatedDailyClubService::postNewStage);
        }
    }
}
