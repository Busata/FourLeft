package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.*;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubResultsService {

    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;



    @Transactional(readOnly = true)
    public Optional<ClubResults> getPreviousResults(String clubId) {
        return clubService.findPreviousEvent(clubId).flatMap(event -> {
            return getResults(clubId, event.getChampionshipID(),event.getId());
        });
    }


    @Transactional(readOnly = true)
    public Optional<ClubResults> getCurrentResults(String clubId) {
        return clubService.getActiveEvent(clubId).flatMap(event -> {
            return getResults(clubId, event.getChampionshipID(), event.getId());
        });
    }

    @Transactional(readOnly = true)
    public Optional<ClubResults> getResults(String clubId, String championshipId, String eventId) {
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
                                return this.buildResults(club, championship, event);
                            });
                });

    }



    private ClubResults buildResults(Club club, Championship championship, Event event) {

//        ChampionshipSettings championshipSettings = championship.getSettings();
        EventSettings eventSettings = event.getEventSettings();

        List<ClubLeaderboardEntry> entries = clubLeaderboardService.findEntries(event.getLeaderboardId());


        return new ClubResults(
                club.getClubName(),
                eventSettings.getLocation(),
                eventSettings.getLocationID(),
                eventSettings.getVehicleClass(),
                eventSettings.getWeatherSeason(),
                event.getLastLeaderboardUpdate(),
                event.getAbsoluteCloseDate(),
                event.getStages().stream().map(Stage::getStageSettings).map(StageSettings::getRoute).toList(),
                entries
        );

    }

    @Transactional(readOnly = true)
    public List<ChampionshipStanding> getStandings(String clubId) {
        Club club = clubService.findById(clubId);
        return club.getActiveChampionshipSnapshot()
                .filter(Championship::hasFinishedEvent)
                .or(() -> clubService.getPreviousChampionship(club))
                .map(Championship::getStandings)
                .stream()
                .flatMap(Collection::stream).toList();
    }

}
