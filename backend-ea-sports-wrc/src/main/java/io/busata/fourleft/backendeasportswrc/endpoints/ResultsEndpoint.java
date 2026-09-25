package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubResultsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubStandingsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubStatsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.TimeTrialTopMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelResultsService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResultsService;
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
    private final ChannelResultsService channelResultsService;

    private final ClubResultsMessageFactory clubResultsMessageFactory;
    private final ClubStandingsMessageFactory standingsMessageFactory;
    private final ClubStatsMessageFactory clubStatsMessageFactory;
    private final TimeTrialTopMessageFactory timeTrialTopMessageFactory;


    @GetMapping("/api_v2/results/{channelId}/current")
    String getCurrentResults(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();
        return channelResultsService.getCurrentResults(discordClubConfiguration).map(results -> clubResultsMessageFactory.createResultPost(results, discordClubConfiguration)).map(MessageEmbed::toData)
                .map(DataObject::toString)
                .orElse("");
    }


    /**
     * Time-trial top 10 (target times) for the channel's current event. Served regardless of the
     * auto-post toggle — the slash command is on demand — but honors the tracked-only setting. A mixed
     * channel has none: time trial boards are per car class.
     */
    @GetMapping("/api_v2/results/{channelId}/timetrial")
    String getTimeTrialTop(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();
        if (channelResultsService.isMixed(discordClubConfiguration)) {
            return "";
        }
        return clubResultsService.getCurrentResults(discordClubConfiguration.getPrimaryClubId())
                .flatMap(results -> timeTrialTopMessageFactory.createTopPost(results, discordClubConfiguration.isTimeTrialTopTrackedOnly()))
                .map(MessageEmbed::toData)
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
        return channelResultsService.getPreviousResults(discordClubConfiguration).map(results -> clubResultsMessageFactory.createResultPost(results, discordClubConfiguration)).map(MessageEmbed::toData).map(DataObject::toString)
               .orElse("");
    }

    @GetMapping("/api_v2/results/{channelId}/stats")
    String getStats(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();

        return channelResultsService.getStats(discordClubConfiguration).map(results -> clubStatsMessageFactory.createPost(results, discordClubConfiguration)).map(MessageEmbed::toData).map(DataObject::toString).orElse("");
    }

    @GetMapping("/api_v2/results/{channelId}/standings")
    String getStandings(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();

        return standingsMessageFactory.createSectionedStandingsPost(channelResultsService.getStandings(discordClubConfiguration), discordClubConfiguration.isRequiresTracking()).toData().toString();
    }
}
