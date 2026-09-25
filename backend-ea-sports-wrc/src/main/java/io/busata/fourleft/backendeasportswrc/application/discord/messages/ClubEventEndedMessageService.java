package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.api.easportswrc.events.ClubEventEnded;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.ChannelClubCompatibilityService;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelResultsService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelResultsService.StandingsSection;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
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
public class ClubEventEndedMessageService {

    private final DiscordGateway discordGateway;

    private final DiscordClubConfigurationService discordClubConfigurationService;
    private final ChannelClubCompatibilityService compatibilityService;
    private final ChannelResultsService channelResultsService;
    private final ChannelPostGate postGate;
    private final ClubService clubService;

    private final ClubResultsMessageFactory clubResultsMessageFactory;
    private final ClubStandingsMessageFactory clubStandingsMessageFactory;
    private final ClubStatsMessageFactory clubStatsMessageFactory;
    private final TimeTrialTopMessageFactory timeTrialTopMessageFactory;

    @EventListener
    public void handleClubEvent(ClubEventEnded eventEnded) {
        discordClubConfigurationService.findPostingForClub(eventEnded.clubId()).forEach(configuration -> {
            try {
                if (channelResultsService.isMixed(configuration) && !lastClubToFinish(configuration, eventEnded.clubId())) {
                    return;
                }
                postClubEventMessages(configuration);
            } catch (Exception ex) {
                log.error("Failed to post club event ended for configuration {}", configuration.getChannelId(), ex);
            }
        });
    }

    /**
     * A MIXED channel posts once for all its clubs: each club's event end is recorded against the primary
     * club's matching event, and only the club completing the set goes on to post.
     */
    private boolean lastClubToFinish(DiscordClubConfiguration configuration, String clubId) {
        Optional<Event> primaryEvent = clubService.findPreviousEvent(clubId).flatMap(event ->
                clubId.equals(configuration.getPrimaryClubId())
                        ? Optional.of(event)
                        : compatibilityService.matchingEvent(configuration.getPrimaryClubId(), event));

        if (primaryEvent.isEmpty()) {
            log.warn("Channel {}: no primary event matches the event club {} just finished; not posting", configuration.getChannelId(), clubId);
            return false;
        }
        return postGate.arrive(configuration.getChannelId(), "event-ended:" + primaryEvent.get().getId(), clubId, configuration.getClubIds());
    }

    private void postClubEventMessages(DiscordClubConfiguration configuration) {
        boolean mixed = channelResultsService.isMixed(configuration);
        List<MessageEmbed> embeds = new ArrayList<>();
        // Post previous results
        channelResultsService.getPreviousResults(configuration).ifPresent(results -> {
            MessageEmbed resultPost = clubResultsMessageFactory.createResultPost(results, configuration);
            embeds.add(resultPost);

        });
        // Post Standings
        List<StandingsSection> standings = channelResultsService.getStandings(configuration);
        if (standings.stream().anyMatch(section -> !section.standings().isEmpty())) {
            MessageEmbed standingsPost = clubStandingsMessageFactory.createSectionedStandingsPost(standings,
                    configuration.isRequiresTracking());
            embeds.add(standingsPost);
        }

        //Stats
        try {
            channelResultsService.getStats(configuration).ifPresent(stats -> {
                try {
                    MessageEmbed statsPost = clubStatsMessageFactory.createPost(stats, configuration);
                    embeds.add(statsPost);
                } catch (Exception ex) {
                    log.error("Could not add statistics to club event ended post", ex);
                }
            });
        } catch (Exception ex) {
            log.error("Could not add statistics to club event ended post", ex);
        }

        // Post new results
        try {
            channelResultsService.getCurrentResults(configuration).ifPresent(results -> {
                MessageEmbed resultPost = clubResultsMessageFactory.createResultPost(results, configuration);
                embeds.add(resultPost);

                // Show the time-trial top 10 so members know the target times to beat, when enabled. Its board
                // is per car class, so a mixed channel has none.
                if (configuration.isTimeTrialTopEnabled() && !mixed) {
                    timeTrialTopMessageFactory.createTopPost(results, configuration.isTimeTrialTopTrackedOnly()).ifPresent(embeds::add);
                }
            });
        } catch (Exception ex) {
            log.error("Could not add current results to club event ended post", ex);
        }

        embeds.forEach(embed -> {
            discordGateway.createMessage(configuration.getChannelId(),
                    new SimpleDiscordMessageTo(null, List.of(embed.toData().toString())));
        });
    }
}
