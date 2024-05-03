package io.busata.fourleft.backendeasportswrc.application.discord.messages;


import io.busata.fourleft.api.easportswrc.events.ClubEventEnded;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResultsService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubStatsService;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClubEventEndedMessageService {

    private final DiscordGateway discordGateway;

    private final DiscordClubConfigurationService discordClubConfigurationService;
    private final ClubResultsService clubResultsService;
    private final ClubStatsService clubStatsService;

    private final ClubResultsMessageFactory clubResultsMessageFactory;
    private final ClubStandingsMessageFactory clubStandingsMessageFactory;
    private final ClubStatsMessageFactory clubStatsMessageFactory;


    @EventListener
    public void handleClubEvent(ClubEventEnded eventEnded) {
        discordClubConfigurationService.findByClubId(eventEnded.clubId()).forEach(configuration -> {
            try {
                postClubEventMessages(eventEnded, configuration);
            } catch(Exception ex) {
                log.error("Failed to post club event ended for configuration {}", configuration.getChannelId());
            }
        });
    }

    private void postClubEventMessages(ClubEventEnded eventEnded, DiscordClubConfiguration configuration) {
        List<MessageEmbed> embeds = new ArrayList<>();
        // Post previous results
        clubResultsService.getPreviousResults(eventEnded.clubId()).ifPresent(results -> {
            MessageEmbed resultPost = clubResultsMessageFactory.createResultPost(results, configuration);
            embeds.add(resultPost);

        });
        // Post Standings
        List<ChampionshipStanding> standings = clubResultsService.getStandings(eventEnded.clubId()).stream().sorted(Comparator.comparing(ChampionshipStanding::getRank)).toList();
        if (!standings.isEmpty()) {
            MessageEmbed standingsPost = clubStandingsMessageFactory.createStandingsPost(standings, configuration.isRequiresTracking());
            embeds.add(standingsPost);
        }

        clubStatsService.buildStats(eventEnded.clubId()).ifPresent(stats -> {
            try {
                MessageEmbed statsPost = clubStatsMessageFactory.createPost(stats, configuration);
                embeds.add(statsPost);
            } catch (Exception ex) {
                log.error("Could not add statistics to club event ended post", ex);
            }
        });

        // Post new results
        clubResultsService.getCurrentResults(eventEnded.clubId()).ifPresent(results -> {
            MessageEmbed resultPost = clubResultsMessageFactory.createResultPost(results, configuration);
            embeds.add(resultPost);
        });

        embeds.forEach(embed -> {
            discordGateway.createMessage(configuration.getChannelId(), new SimpleDiscordMessageTo(null, List.of(embed.toData().toString())));
        });
    }
}
