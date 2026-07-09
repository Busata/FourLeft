package io.busata.fourleft.backendeasportswrc.domain.services;

import io.busata.fourleft.api.easportswrc.models.ChampionshipSettingsTo;
import io.busata.fourleft.api.easportswrc.models.ClubChampionshipResultTo;
import io.busata.fourleft.api.easportswrc.models.ClubEventResultTo;
import io.busata.fourleft.api.easportswrc.models.ClubOverviewTo;
import io.busata.fourleft.api.easportswrc.models.ClubResultEntryTo;
import io.busata.fourleft.api.easportswrc.models.EventRestrictionTo;
import io.busata.fourleft.api.easportswrc.models.EventSettingsTo;
import io.busata.fourleft.api.easportswrc.models.StageSettingsTo;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.domain.services.restrictions.RestrictionService;
import io.busata.fourleft.common.RestrictionDisplayMode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOverviewService {

    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;
    private final DiscordClubConfigurationService discordClubConfigurationService;
    private final RestrictionService restrictionService;

    @Transactional
    public ClubOverviewTo createOverview(String clubId) {
        return createOverview(clubId, null);
    }

    @Transactional
    public ClubOverviewTo createOverview(String clubId, Integer maxChampionships) {
        Club club = clubService.findById(clubId);

        // Filter championships based on limit
        List<Championship> championships = club.getChampionships();
        if (maxChampionships != null && maxChampionships > 0) {
            championships = championships.stream()
                    .sorted(Comparator.comparing(Championship::getAbsoluteCloseDate).reversed())
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

        // Union of restriction rules across all of the club's channel configs; on conflicting
        // duplicates for the same target, first-found wins.
        List<EventRestriction> restrictionRules = discordClubConfigurationService.findByClubId(clubId).stream()
                .flatMap(config -> config.getEventRestrictionsOrEmpty().stream())
                .toList();

        List<ClubChampionshipResultTo> championshipResults = championships.stream().map(championship -> {

            List<ClubEventResultTo> events = championship.getEvents().stream().map(event -> {
                Stage lastStage = event.getLastStage();

                // Use the pre-fetched map for O(1) lookup instead of N queries
                List<ClubLeaderboardEntry> entries = leaderboardEntriesMap.getOrDefault(
                        lastStage.getLeaderboardId(),
                        List.of()
                );

                Optional<EventRestriction> restriction =
                        restrictionService.resolveRestriction(restrictionRules, championship.getId(), event.getId());

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
                        createEntries(entries, restriction),
                        restriction.map(CustomOverviewService::toRestrictionTo).orElse(null)
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

    /**
     * Display-EXCLUDE violators are filtered out here, server-side, so web, exports and Discord agree;
     * the survivors are re-ranked 1..n and their deltas recomputed against the new leader. WARN
     * violators stay in but are flagged.
     */
    private List<ClubResultEntryTo> createEntries(List<ClubLeaderboardEntry> entries, Optional<EventRestriction> restriction) {
        boolean exclude = restriction.map(rule -> rule.displayMode() == RestrictionDisplayMode.EXCLUDE).orElse(false);
        boolean warn = restriction.map(rule -> rule.displayMode() == RestrictionDisplayMode.WARN).orElse(false);

        if (!exclude) {
            return entries.stream().map(entry -> toEntryTo(
                    entry,
                    entry.getRank(),
                    entry.getDifferenceToFirst(),
                    entry.getDifferenceAccumulated(),
                    warn && restrictionService.violates(restriction.get(), entry) ? true : null
            )).toList();
        }

        List<ClubLeaderboardEntry> visible = entries.stream()
                .filter(entry -> !restrictionService.violates(restriction.get(), entry))
                .sorted(Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated))
                .toList();

        Optional<ClubLeaderboardEntry> leader = visible.stream().findFirst();

        List<ClubResultEntryTo> result = new ArrayList<>(visible.size());
        for (int i = 0; i < visible.size(); i++) {
            ClubLeaderboardEntry entry = visible.get(i);
            result.add(toEntryTo(
                    entry,
                    (long) i + 1,
                    recomputeDelta(entry.getTime(), leader.map(ClubLeaderboardEntry::getTime).orElse(null)),
                    recomputeDelta(entry.getTimeAccumulated(), leader.map(ClubLeaderboardEntry::getTimeAccumulated).orElse(null)),
                    null
            ));
        }
        return result;
    }

    private static Duration recomputeDelta(Duration time, Duration leaderTime) {
        if (time == null || leaderTime == null) {
            return null;
        }
        return time.minus(leaderTime);
    }

    private static ClubResultEntryTo toEntryTo(ClubLeaderboardEntry entry, Long rank, Duration differenceToFirst,
                                               Duration differenceAccumulated, Boolean restrictionViolated) {
        return new ClubResultEntryTo(
                entry.getWrcPlayerId(),
                entry.getDisplayName(),
                entry.getNationalityID(),
                entry.getPlatform(),
                rank,
                entry.getVehicle(),
                entry.getTime(),
                entry.getTimeAccumulated(),
                entry.getTimePenalty(),
                differenceToFirst,
                differenceAccumulated,
                restrictionViolated
        );
    }

    private static EventRestrictionTo toRestrictionTo(EventRestriction restriction) {
        return new EventRestrictionTo(
                restriction.type(),
                restriction.championshipId(),
                restriction.eventId(),
                restriction.displayMode(),
                restriction.scoringMode(),
                restriction.penaltyPoints(),
                restriction.allowedVehicles()
        );
    }
}
