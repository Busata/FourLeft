package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.*;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.domain.services.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubResultsService {

    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;
    private final ProfileService profileService;



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



    private ClubResults buildResults(Club club, Championship championship, Event event) {

        EventSettings eventSettings = event.getEventSettings();

        List<ClubLeaderboardEntry> entries = clubLeaderboardService.findEntries(event.getLeaderboardId());


        Stage lastStage = event.getStages().get(event.getStages().size() - 1);

        return new ClubResults(
                club.getId(),
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
    public List<ChampionshipStanding> getStandings(String clubId) {
        Club club = clubService.findById(clubId);

        // if(Objects.equals(clubId, "146")) {
        //     log.info("Calculating points for club 146");

        //     return club.getActiveChampionshipSnapshot()
        //     .filter(Championship::hasFinishedEvent)
        //     .or(() -> clubService.getPreviousChampionship(club))
        //     .map(this::createCustomStandingsForWeeklyPowerStage)
        //             .orElse(List.of());
        // }

        return club.getActiveChampionshipSnapshot()
                .filter(Championship::hasFinishedEvent)
                .or(() -> clubService.getPreviousChampionship(club))
                .map(Championship::getStandings)
                .stream()
                .flatMap(Collection::stream).toList();
    }

    @NotNull
    private List<ChampionshipStanding> createCustomStandingsForWeeklyPowerStage(Championship championship) {
        final Map<String, PlayerEntryData> playerData = new HashMap<>();
        final Map<String, Integer> points = new HashMap<>();

        championship.getEvents().stream()
        .filter(Event::isFinished)
        .forEach(event -> {
            String leaderboardId = event.getLastStage().getLeaderboardId();
            var board = clubLeaderboardService.findById(leaderboardId);

            board.getEntries().stream().sorted(Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated)).forEach(entry -> {

                playerData.computeIfAbsent(entry.getSsid(), (ssid) -> {
                    return new PlayerEntryData(entry.getSsid(), entry.getDisplayName(), entry.getNationalityID().intValue());
                });

                points.putIfAbsent(entry.getSsid(), 0);

                points.computeIfPresent(entry.getSsid(), (ssid, oldPoints) -> {
                    if(entry.isDnf()) {
                        return oldPoints;
                    }

                    return oldPoints + CustomPoints.getValue(entry.getRankAccumulated().intValue());
                });

            });
        });

        final AtomicInteger ranks = new AtomicInteger(1);

        return points.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).map(entry -> {
            var player = playerData.get(entry.getKey());
            return new ChampionshipStanding(UUID.randomUUID(), player.ssid(), player.displayName(), entry.getValue(), ranks.getAndAdd(1), player.nationalityId());
        }).map(standing -> {
            standing.setProfile(profileService.getProfileById(standing.getSsid()).orElse(null));
            return standing;
        }).toList();
    }

}
