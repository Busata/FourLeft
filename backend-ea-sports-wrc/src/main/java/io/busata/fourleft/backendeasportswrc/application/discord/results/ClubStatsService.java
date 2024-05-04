package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.models.StageSettings;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubStatsService {

    private final ClubService clubService;
    private final ClubResultsService resultsService;
    private final ClubLeaderboardService clubLeaderboardService;


    public Optional<ClubStats> buildStats(String clubId) {
//        List<ChampionshipStanding> standings = resultsService.getStandings(clubId);

        return clubService.findPreviousEvent(clubId).flatMap(event -> {
            return resultsService.getResults(clubId, event.getChampionshipID(), event.getId(), (club, championship, event1) -> {
                List<ClubLeaderboardEntry> entries = clubLeaderboardService.findEntries(event1.getLeaderboardId());


                CarStatistics carStatistics = calculateCarStatistics(entries);
                PlayerStatistics playerStatistics = calculatePlayerStatistics(entries);

                return new ClubStats(
                        event.getEventSettings().getVehicleClass(),
                        event.getEventSettings().getLocation(),
                        event.getEventSettings().getLocationID(),
                        event.getStages().stream().map(Stage::getStageSettings).map(StageSettings::getRoute).toList(),
                        carStatistics,
                        playerStatistics
                );
            });
        });


    }

    @NotNull
    private static CarStatistics calculateCarStatistics(List<ClubLeaderboardEntry> entries) {
        int totalCount = entries.size();
        Map<String, Long> vehicleCount = entries.stream()
                .map(ClubLeaderboardEntry::getVehicle)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Double> percentages = new HashMap<>();
        vehicleCount.forEach((key, value) -> {
            double percentage = (value * 100.0) / totalCount;
            percentages.put(key, percentage);
        });

        Map<String, Long> topCount = entries.stream().sorted(Comparator.comparing(ClubLeaderboardEntry::getRank))
                .limit(10)
                .map(ClubLeaderboardEntry::getVehicle)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return new CarStatistics(percentages, vehicleCount, topCount);
    }

    @NotNull
    private static PlayerStatistics calculatePlayerStatistics(List<ClubLeaderboardEntry> entries) {
        if (entries.isEmpty()) {
            return new PlayerStatistics(0, 0, 0, 0);
        }

        long totalEntries = entries.size();
        long totalDnf = entries.stream().filter(entry -> entry.isDnf()).count();
        long percentageDnf = (totalDnf * 100) / totalEntries;
        long percentageFinished = 100 - percentageDnf;

        return new PlayerStatistics(totalEntries, totalDnf, percentageDnf, percentageFinished);
    }

}
