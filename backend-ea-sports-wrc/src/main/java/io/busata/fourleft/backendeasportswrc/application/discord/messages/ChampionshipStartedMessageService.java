package io.busata.fourleft.backendeasportswrc.application.discord.messages;


import io.busata.fourleft.api.easportswrc.events.ClubChampionshipStarted;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.ChannelClubCompatibilityService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelResultsService;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.services.championships.ChampionshipService;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChampionshipStartedMessageService {

    private final DiscordGateway discordGateway;

    private final DiscordClubConfigurationService discordClubConfigurationService;
    private final ChannelResultsService channelResultsService;
    private final ChannelClubCompatibilityService compatibilityService;
    private final ChannelPostGate postGate;
    private final ClubService clubService;
    private final ClubEventsMessageFactory clubEventsMessageFactory;
    private final ChampionshipService championshipService;

    private final ClubResultsMessageFactory clubResultsMessageFactory;
    private final TimeTrialTopMessageFactory timeTrialTopMessageFactory;

    @EventListener
    public void handleClubEvent(ClubChampionshipStarted championshipStarted) {
        String clubId = championshipStarted.clubId();
        discordClubConfigurationService.findPostingForClub(clubId).forEach(configuration -> {
            try {
                if (channelResultsService.isMixed(configuration) && !lastClubToStart(configuration, clubId)) {
                    return;
                }
                post(configuration);
            } catch (Exception ex) {
                log.error("Failed to post championship started for configuration {}", configuration.getChannelId(), ex);
            }
        });
    }

    /** A MIXED channel posts once for all its clubs, keyed on the primary club's matching championship. */
    private boolean lastClubToStart(DiscordClubConfiguration configuration, String clubId) {
        Optional<Championship> primaryChampionship = clubService.getActiveChampionshipId(clubId)
                .flatMap(championshipService::findChampionship)
                .flatMap(championship -> clubId.equals(configuration.getPrimaryClubId())
                        ? Optional.of(championship)
                        : compatibilityService.matchingChampionship(configuration.getPrimaryClubId(), championship));

        if (primaryChampionship.isEmpty()) {
            log.warn("Channel {}: no primary championship matches the one club {} just started; not posting", configuration.getChannelId(), clubId);
            return false;
        }
        return postGate.arrive(configuration.getChannelId(), "championship-started:" + primaryChampionship.get().getId(), clubId, configuration.getClubIds());
    }

    private void post(DiscordClubConfiguration configuration) {
        boolean mixed = channelResultsService.isMixed(configuration);
        List<MessageEmbed> embeds = new ArrayList<>();

        clubService.getActiveChampionshipId(configuration.getPrimaryClubId()).flatMap(championshipService::findChampionship).ifPresent(championship -> {
            MessageEmbed post = clubEventsMessageFactory.createEventSummary(championship, channelResultsService.summaryClasses(configuration, championship));
            embeds.add(post);
        });

        channelResultsService.getCurrentResults(configuration).ifPresent(results -> {
            MessageEmbed resultPost = clubResultsMessageFactory.createResultPost(results, configuration);
            embeds.add(resultPost);

            // Show the time-trial top 10 so members know the target times to beat, when enabled. Its board
            // is per car class, so a mixed channel has none.
            if (configuration.isTimeTrialTopEnabled() && !mixed) {
                timeTrialTopMessageFactory.createTopPost(results, configuration.isTimeTrialTopTrackedOnly()).ifPresent(embeds::add);
            }
        });

        embeds.forEach(embed -> {
            discordGateway.createMessage(configuration.getChannelId(), new SimpleDiscordMessageTo(null, List.of(embed.toData().toString())));
        });
    }
}
