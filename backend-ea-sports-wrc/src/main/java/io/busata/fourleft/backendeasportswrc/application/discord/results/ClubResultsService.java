package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.*;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.domain.services.profile.ProfileService;
import io.busata.fourleft.backendeasportswrc.domain.services.restrictions.RestrictionService;
import io.busata.fourleft.common.RestrictionScoringMode;
import io.busata.fourleft.common.ScoringStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubResultsService {

    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;
    private final ProfileService profileService;
    private final ScoringService scoringService;
    private final RestrictionService restrictionService;



    @Transactional(readOnly = true)
    public Optional<ClubResults> getPreviousResults(String clubId) {
        return clubService.findPreviousEvent(clubId).flatMap(event -> {
            return getResults(clubId, event.getChampionshipID(),event.getId(), this::buildResults);
        });
    }


    @Transactional(readOnly = true)
    public Optional<ClubResults> getCurrentResults(String clubId) {
        return clubService.getActiveEvent(clubId).flatMap(event -> {
            return getResults(clubId, event.getChampionshipID(), event.getId(), this::buildResults);
        });
    }

    @Transactional(readOnly = true)
    public List<ClubResults> getEventResults(String clubId, String championshipId) {
        return getResults(clubId, championshipId, this::buildResults);
    }

    @Transactional(readOnly = true)
    public <T> Optional<T> getResults(String clubId, String championshipId, String eventId, ResultBuilder<T> builder) {
        Club club = clubService.findById(clubId);

        return club.getChampionships()
                .stream()
                .filter(championship -> Objects.equals(championship.getId(), championshipId))
                .findFirst()
                .flatMap(championship -> {
                    return championship.getEvents()
                            .stream()
                            .filter(event -> Objects.equals(event.getId(), eventId))
                            .findFirst()
                            .map(event -> {
                                return builder.accept(club, championship, event);
                            });
                });
    }

    @Transactional(readOnly = true)
    public <T> List<T> getResults(String clubId, String championshipId, ResultBuilder<T> builder) {
        Club club = clubService.findById(clubId);

        return club.getChampionships()
                .stream()
                .filter(championship -> Objects.equals(championship.getId(), championshipId))
                .findFirst()
                .stream().flatMap(championship -> {
                    return championship.getEvents()
                            .stream()
                            .filter(Event::isFinished)
                            .map(event -> {
                                return builder.accept(club, championship, event);
                            });
                }).toList();
    }



    private ClubResults buildResults(Club club, Championship championship, Event event) {

        EventSettings eventSettings = event.getEventSettings();

        List<ClubLeaderboardEntry> entries = clubLeaderboardService.findEntries(event.getLeaderboardId());


        Stage lastStage = event.getStages().get(event.getStages().size() - 1);

        return new ClubResults(
                club.getId(),
                championship.getId(),
                event.getId(),
                club.getClubName(),
                eventSettings.getLocation(),
                eventSettings.getLocationID(),
                lastStage.getStageSettings().getRouteID(),
                eventSettings.getVehicleClass(),
                eventSettings.getVehicleClassID(),
                eventSettings.getWeatherSeason(),
                eventSettings.getWeatherSeasonID(),
                lastStage.getStageSettings().getWeatherAndSurface(),
                lastStage.getStageSettings().getWeatherAndSurfaceID(),
                event.getLastLeaderboardUpdate(),
                event.getAbsoluteCloseDate(),
                event.getStages().stream().map(Stage::getStageSettings).map(StageSettings::getRoute).toList(),
                entries
        );

    }

    @Transactional(readOnly = true)
    public List<ChampionshipStanding> getStandings(DiscordClubConfiguration configuration) {
        String clubId = configuration.getClubId();
        Club club = clubService.findById(clubId);

         if(configuration.isCustomScoringEnabled()) {
             log.info("Calculating custom points for club {}", clubId);

             return club.getActiveChampionshipSnapshot()
             .filter(Championship::hasFinishedEvent)
             .or(() -> clubService.getPreviousChampionship(club))
             .map(championship -> createCustomStandings(championship, configuration))
                     .orElse(List.of());
         }

        return club.getActiveChampionshipSnapshot()
                .filter(Championship::hasFinishedEvent)
                .or(() -> clubService.getPreviousChampionship(club))
                .map(Championship::getStandings)
                .stream()
                .flatMap(Collection::stream).toList();
    }

    @NotNull
    private List<ChampionshipStanding> createCustomStandings(Championship championship, DiscordClubConfiguration configuration) {
        final Map<String, PlayerEntryData> playerData = new HashMap<>();
        final Map<String, ChampionshipStanding> standings = new HashMap<>();

        List<Event> events =  championship.getEvents().stream()
        .filter(Event::isFinished)
        .toList();

        for(Event event: events) {
            var points = calculateEventPoints(event, playerData, configuration);

            points.entrySet().forEach(entrySet -> {
                if(entrySet.getKey() == null) {
                    return;
                }

                standings.computeIfPresent(entrySet.getKey(), (ssid, standing) -> {
                    var actualPoints = points.get(ssid);
                    standing.updatePoints(actualPoints + standing.getPointsAccumulated());;
                    return standing;
                });

                standings.computeIfAbsent(entrySet.getKey(), (ssid) -> {
                    var player = playerData.get(ssid);
                    var actualPoints = points.get(ssid);
                    var profile = profileService.getProfileById(ssid).orElse(null);
                    ChampionshipStanding championshipStanding = new ChampionshipStanding(UUID.randomUUID(), player.ssid(), player.displayName(), actualPoints, 0, player.nationalityId());
                    championshipStanding.setProfile(profile);
                    return championshipStanding;
                });
            });

            var ranks = calculateRanks(standings);

            ranks.entrySet().forEach(entrySet -> {
                standings.computeIfPresent(entrySet.getKey(), (key, standing) -> {
                    standing.updateRank(entrySet.getValue());
                    return standing;
                });
            });
        };

        return standings.values().stream().sorted(Comparator.comparing(ChampionshipStanding::getRank)).toList();
    }
    
    private Map<String, Integer> calculateRanks(Map<String, ChampionshipStanding> standings) {
        var sortedStandings = standings.values().stream().sorted(Comparator.comparing(ChampionshipStanding::getPointsAccumulated).reversed()).toList();

        Map<String, Integer> ranks = new HashMap<>();

        for(int i = 0; i < sortedStandings.size(); i++) {
            ranks.put(sortedStandings.get(i).getSsid(), i + 1);
        }

        return ranks;
    }

    private Map<String, Integer> calculateEventPoints(Event event, Map<String, PlayerEntryData> playerData, DiscordClubConfiguration configuration) {
        Map<String, Integer> points = new HashMap<>();

            String leaderboardId = event.getLastStage().getLeaderboardId();
            var board = clubLeaderboardService.findById(leaderboardId);

            boolean racenetDefault = configuration.getScoringStrategy() == ScoringStrategy.RACENET_DEFAULT;

            // Racenet's default formula scales with everyone who STARTED the event, and still awards
            // mid-event dropouts the tail of the curve — both live only on the earlier stage boards,
            // so those are only pulled in for that strategy. DNFs on the final board are ranked and
            // scored by racenet too (the curve bottoms out at 0 anyway), so they aren't skipped there.
            List<ClubLeaderboardEntry> dropouts = racenetDefault ? findDropouts(event, board.getEntries()) : List.of();
            int fieldSize = board.getEntries().size() + dropouts.size();

            Optional<EventRestriction> restriction = restrictionService.resolveRestriction(configuration, event.getChampionshipID(), event.getId());

            Map<ClubLeaderboardEntry, Long> excludedScoringPositions = restriction
                    .filter(rule -> rule.scoringMode() == RestrictionScoringMode.EXCLUDE)
                    .map(rule -> restrictionService.scoringPositions(rule, board.getEntries()))
                    .orElse(null);

            board.getEntries().stream().sorted(Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated)).forEach(entry -> {

                playerData.computeIfAbsent(entry.getSsid(), (ssid) -> {
                    return new PlayerEntryData(entry.getSsid(), entry.getDisplayName(), entry.getNationalityID().intValue());
                });

                points.putIfAbsent(entry.getSsid(), 0);

                points.computeIfPresent(entry.getSsid(), (ssid, oldPoints) -> {
                    if(entry.isDnf() && !racenetDefault) {
                        return oldPoints;
                    }

                    boolean violates = restriction.map(rule -> restrictionService.violates(rule, entry)).orElse(false);

                    if (violates && excludedScoringPositions != null) {
                        return oldPoints;
                    }

                    long position = excludedScoringPositions != null
                            ? excludedScoringPositions.get(entry)
                            : entry.getRankAccumulated();

                    int entryPoints = scoringService.getPoints(configuration, (int) position, fieldSize);

                    if (violates) {
                        entryPoints = Math.max(0, entryPoints - Optional.ofNullable(restriction.get().penaltyPoints()).orElse(0));
                    }

                    return oldPoints + entryPoints;
                });

            });

            long dropoutPosition = board.getEntries().size();
            for (ClubLeaderboardEntry entry : dropouts) {
                dropoutPosition++;

                playerData.computeIfAbsent(entry.getSsid(), (ssid) -> {
                    return new PlayerEntryData(entry.getSsid(), entry.getDisplayName(), entry.getNationalityID().intValue());
                });

                points.putIfAbsent(entry.getSsid(), 0);

                boolean violates = restriction.map(rule -> restrictionService.violates(rule, entry)).orElse(false);
                if (violates && excludedScoringPositions != null) {
                    continue;
                }

                int entryPoints = scoringService.getPoints(configuration, (int) dropoutPosition, fieldSize);
                if (violates) {
                    entryPoints = Math.max(0, entryPoints - Optional.ofNullable(restriction.get().penaltyPoints()).orElse(0));
                }

                points.merge(entry.getSsid(), entryPoints, Integer::sum);
            }

            return points;
    }

    /**
     * Players who started the event but never reached the final board (quit mid-event). Racenet ranks
     * them below every finisher — furthest stage reached first, then accumulated time — which is the
     * order its default scoring hands out the remaining tail points in.
     */
    private List<ClubLeaderboardEntry> findDropouts(Event event, List<ClubLeaderboardEntry> finishers) {
        List<Stage> stages = event.getStages();
        if (stages.size() <= 1) {
            return List.of();
        }

        Set<String> finisherIds = finishers.stream().map(ClubLeaderboardEntry::getSsid).collect(Collectors.toSet());

        List<String> earlierBoardIds = stages.subList(0, stages.size() - 1).stream()
                .map(Stage::getLeaderboardId)
                .toList();
        Map<String, List<ClubLeaderboardEntry>> stageBoards = clubLeaderboardService.findEntriesByLeaderboardIds(earlierBoardIds);

        // Walking the stages in running order leaves each dropout's furthest-stage entry behind.
        Map<String, ClubLeaderboardEntry> lastEntry = new HashMap<>();
        Map<String, Integer> lastStageIndex = new HashMap<>();
        for (int stageIndex = 0; stageIndex < earlierBoardIds.size(); stageIndex++) {
            for (ClubLeaderboardEntry entry : stageBoards.getOrDefault(earlierBoardIds.get(stageIndex), List.of())) {
                if (finisherIds.contains(entry.getSsid())) {
                    continue;
                }
                lastEntry.put(entry.getSsid(), entry);
                lastStageIndex.put(entry.getSsid(), stageIndex);
            }
        }

        return lastEntry.values().stream()
                .sorted(Comparator.<ClubLeaderboardEntry>comparingInt(entry -> -lastStageIndex.get(entry.getSsid()))
                        .thenComparing(ClubLeaderboardEntry::getTimeAccumulated))
                .toList();
    }

}
