package io.busata.fourleft.backendeasportswrc.domain.services;

import io.busata.fourleft.api.easportswrc.models.*;
import io.busata.fourleft.backendeasportswrc.application.importer.ClubFactory;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOverviewService {

    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;

    @Transactional
    public ClubOverviewTo createOverview(String clubId) {
        return createOverview(clubId, null);
    }

    @Transactional
    public ClubOverviewTo createOverview(String clubId, Integer maxChampionships) {
        Club club = clubService.findById(clubId);

        // Filter championships based on limit
        List<io.busata.fourleft.backendeasportswrc.domain.models.Championship> championships = club.getChampionships();
        if (maxChampionships != null && maxChampionships > 0) {
            championships = championships.stream()
                    .sorted(Comparator.comparing(io.busata.fourleft.backendeasportswrc.domain.models.Championship::getAbsoluteCloseDate).reversed())
                    .limit(maxChampionships)
                    .collect(Collectors.toList());
        }

        // Collect all leaderboard IDs upfront to enable batch fetching
        List<String> allLeaderboardIds = championships.stream()
                .flatMap(championship -> championship.getEvents().stream())
                .map(event -> event.getLastStage().getLeaderboardId())
                .toList();

        // Batch fetch all leaderboard entries in a single query
        Map<String, List<ClubLeaderboardEntry>> leaderboardEntriesMap =
                clubLeaderboardService.findEntriesByLeaderboardIds(allLeaderboardIds);

        List<ClubChampionshipResultTo> championshipResults = championships.stream().map(championship -> {

            List<ClubEventResultTo> events = championship.getEvents().stream().map(event -> {
                Stage lastStage = event.getLastStage();

                // Use the pre-fetched map for O(1) lookup instead of N queries
                List<ClubLeaderboardEntry> entries = leaderboardEntriesMap.getOrDefault(
                        lastStage.getLeaderboardId(),
                        List.of()
                );

                return new ClubEventResultTo(
                        new EventSettingsTo(
                                event.getEventSettings().getLocation(),
                                event.getEventSettings().getWeatherSeason(),
                                event.getEventSettings().getVehicleClass(),
                                event.getEventSettings().getDuration()
                        ),
                        event.getStages().stream().map(Stage::getStageSettings)
                                .map(settings -> {
                                    return new StageSettingsTo(
                                            settings.getRoute(),
                                            settings.getTimeOfDay(),
                                            settings.getServiceAre(),
                                            settings.getWeatherAndSurface()
                                    );
                                })
                                .toList(),
                        event.getId(),
                        event.getStatus().toString(),
                        event.getAbsoluteOpenDate(),
                        event.getAbsoluteCloseDate(),
                        entries.stream().map(entry -> {
                            return new ClubResultEntryTo(
                                    entry.getWrcPlayerId(),
                                    entry.getDisplayName(),
                                    entry.getNationalityID(),
                                    entry.getPlatform(),
                                    entry.getRank(),
                                    entry.getVehicle(),
                                    entry.getTime(),
                                    entry.getTimeAccumulated(),
                                    entry.getTimePenalty(),
                                    entry.getDifferenceToFirst(),
                                    entry.getDifferenceAccumulated()
                            );
                        }).toList()
                );


            }).toList();

            return new ClubChampionshipResultTo(
                    championship.getId(),
                    championship.getStatus().toString(),
                    championship.getAbsoluteOpenDate(),
                    championship.getAbsoluteCloseDate(),
                    new ChampionshipSettingsTo(
                            championship.getSettings().getName(),
                            championship.getSettings().getFormat(),
                            championship.getSettings().getBonusPointsMode(),
                            championship.getSettings().getScoringSystem(),
                            championship.getSettings().getIsTuningAllowe(),
                            championship.getSettings().getIsHardcoreDamageEnabled(),
                            championship.getSettings().getTrackDegradation(),
                            championship.getSettings().getIsAssistsAllowed()
                    ),
                    events
            );
        }).toList();

        return new ClubOverviewTo(club.getId(), club.getClubName(), club.getClubDescription(), club.getClubCreatedAt(), club.getActiveMemberCount(), club.getLastLeaderboardUpdate(), club.getLastDetailsUpdate(), championshipResults);
    }
}
