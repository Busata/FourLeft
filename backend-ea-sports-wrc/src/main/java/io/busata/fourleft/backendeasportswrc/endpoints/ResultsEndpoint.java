package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubResultsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubStandingsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubStatsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResultsService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubStatsService;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    @GetMapping(value="/api_v2/results/club/{clubId}/{championshipId}", produces = "text/csv")
    ResponseEntity<String> getClubResults(@PathVariable String clubId, @PathVariable String championshipId) {
        List<ClubResults> eventResults = clubResultsService.getEventResults(clubId, championshipId).stream().sorted(Comparator.comparing(ClubResults::eventCloseDate)).toList();

        var championship = eventResults.get(0).championshipName();

        var entryLists = eventResults.stream().map(result -> result.entries().stream().sorted(Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated)).toList()).toList();

        var maxEntries = entryLists.stream().mapToInt(List::size).max().orElse(0);

        List<String> lines = new ArrayList<>();
        lines.add("Rank,DisplayName,Vehicle,Time,,".repeat(entryLists.size()));

        for (int i = 0; i < maxEntries; i++) {

            StringBuilder builder = new StringBuilder();

            for(var list : entryLists) {
                builder.append(i < list.size() ? buildLine(list.get(i)) : ",,,");
                builder.append(",,");
            }

            builder.deleteCharAt(builder.length() - 1);

            lines.add(builder.toString());
        }


        String csv = lines.stream().collect(Collectors.joining("\n"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+clubId+"_"+championship+".csv\"")
                .body(csv);
    }

    private String buildLine(ClubLeaderboardEntry entry) {
        return "%s,%s,%s,%s".formatted(entry.getRankAccumulated(), entry.getDisplayName(), entry.getVehicle(), DurationHelper.formatTime(entry.getTimeAccumulated()));
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

        return standingsMessageFactory.createStandingsPost(standings, discordClubConfiguration.isRequiresTracking()).toData().toString();
    }
}
