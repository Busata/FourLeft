package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubResultsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubStandingsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubStatsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResultsService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubStatsService;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ResultsEndpoint {

    private final DiscordClubConfigurationService discordClubConfigurationService;

    private final ClubResultsService clubResultsService;
    private final ClubStatsService clubStatsService;

    private final ClubResultsMessageFactory clubResultsMessageFactory;
    private final ClubStandingsMessageFactory standingsMessageFactory;
    private final ClubStatsMessageFactory clubStatsMessageFactory;


    @GetMapping("/api_v2/results/{channelId}/current")
    String getCurrentResults(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();
        return clubResultsService.getCurrentResults(discordClubConfiguration.getClubId()).map(results -> clubResultsMessageFactory.createResultPost(results, discordClubConfiguration)).map(MessageEmbed::toData)
                .map(DataObject::toString)
                .orElse("");
    }

    @GetMapping("/api_v2/results/{channelId}/previous")
    String getPreviousResults(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();
        return clubResultsService.getPreviousResults(discordClubConfiguration.getClubId()).map(results -> clubResultsMessageFactory.createResultPost(results, discordClubConfiguration)).map(MessageEmbed::toData).map(DataObject::toString)
               .orElse("");
    }

    @GetMapping("/api_v2/results/{channelId}/stats")
    String getStats(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();

        return clubStatsService.buildStats(discordClubConfiguration.getClubId()).map(results -> clubStatsMessageFactory.createPost(results, discordClubConfiguration)).map(MessageEmbed::toData).map(DataObject::toString).orElse("");
    }

    @GetMapping("/api_v2/results/{channelId}/standings")
    String getStandings(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();

        List<ChampionshipStanding> standings = clubResultsService.getStandings(discordClubConfiguration.getClubId()).stream().sorted(Comparator.comparing(ChampionshipStanding::getRank)).toList();
        log.info("Standings size: {}", standings.size());
        return standingsMessageFactory.createStandingsPost(standings, discordClubConfiguration.isRequiresTracking()).toData().toString();
    }
}
