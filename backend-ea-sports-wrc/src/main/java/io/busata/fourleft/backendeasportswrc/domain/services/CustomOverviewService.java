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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOverviewService {

    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;

    @Transactional
    public ClubOverviewTo createOverview(String clubId) {
        Club club = clubService.findById(clubId);
        List<ClubChampionshipResultTo> championships = club.getChampionships().stream().map(championship -> {

            List<ClubEventResultTo> events = championship.getEvents().stream().map(event -> {
                Stage lastStage = event.getLastStage();


                List<ClubLeaderboardEntry> entries = clubLeaderboardService.findEntries(lastStage.getLeaderboardId());


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

        return new ClubOverviewTo(club.getId(), club.getClubName(), club.getClubDescription(), club.getClubCreatedAt(), club.getActiveMemberCount(), club.getLastLeaderboardUpdate(), club.getLastDetailsUpdate(), championships);
    }
}
