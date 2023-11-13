package io.busata.fourleft.backendeasportswrc.application.importer;

import io.busata.fourleft.backendeasportswrc.domain.models.*;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.*;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultEntryTo;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
public class ClubFactory {

    public Club create(ClubDetailsTo clubDetailsTo, List<ChampionshipTo> championships) {
        Club club = createClub(clubDetailsTo);

        clubDetailsTo
                .currentChampionship()
                .map(this::createChampionship)
                .ifPresent(club::updateChampionship);

        championships.stream().map(this::createChampionship).forEach(club::updateChampionship);

        return club;
    }



    public Club update(Club existingClub, ClubDetailsTo clubDetails, List<ChampionshipTo> championships) {
        existingClub.updateBasicDetails(
                clubDetails.clubName(),
                clubDetails.clubDescription(),
                clubDetails.activeMemberCount()
        );


        clubDetails
                .currentChampionship()
                .map(this::createChampionship)
                .ifPresent(existingClub::updateChampionship);

        championships.stream().map(this::createChampionship).forEach(existingClub::updateChampionship);

        existingClub.getChampionships().forEach(Championship::updateStatus);

        return existingClub;
    }


    private Club createClub(ClubDetailsTo clubDetailsTo) {
        return new Club(
                clubDetailsTo.clubID(),
                clubDetailsTo.clubName(),
                clubDetailsTo.clubDescription(),
                clubDetailsTo.activeMemberCount(),
                parseTimestamp(clubDetailsTo.clubCreatedAt())
        );
    }

    private Championship createChampionship(ChampionshipTo championshipTo) {
        Championship championship = new Championship(
                championshipTo.id(),
                create(championshipTo.settings()),
                parseTimestamp(championshipTo.absoluteOpenDate()),
                parseTimestamp(championshipTo.absoluteCloseDate())
        );

        championship.updateEvents(championshipTo.events().stream().map(this::createEvent).toList());

        return championship;
    }

    private Event createEvent(ChampionshipEventTo eventTo) {
        Event event = new Event(
                eventTo.id(),
                eventTo.leaderboardID(),
                parseTimestamp(eventTo.absoluteOpenDate()),
                parseTimestamp(eventTo.absoluteCloseDate()),
                eventTo.status(),
                create(eventTo.eventSettings())
        );

        event.updateStages(eventTo.stages().stream().map(this::createStage).toList());

        return event;
    }

    private Stage createStage(ChampionshipEventStageTo stageTo) {
        return new Stage(
                stageTo.id(),
                stageTo.leaderboardID(),
                create(stageTo.stageSettings()));
    }

    private EventSettings create(ChampionshipEventSettingsTo championshipEventSettingsTo) {
        return new EventSettings(
                championshipEventSettingsTo.vehicleClassID(),
                championshipEventSettingsTo.vehicleClass(),
                championshipEventSettingsTo.weatherSeasonID(),
                championshipEventSettingsTo.weatherSeason(),
                championshipEventSettingsTo.locationID(),
                championshipEventSettingsTo.location(),
                championshipEventSettingsTo.duration()
        );
    }


    public ChampionshipSettings create(ChampionshipSettingsTo settings) {
        return new ChampionshipSettings(
                settings.name(),
                settings.format(),
                settings.bonusPointsMode(),
                settings.scoringSystem(),
                settings.trackDegradation(),
                settings.isHardcoreDamageEnabled(),
                settings.isAssistsAllowed(),
                settings.isTuningAllowed()
        );
    }

    public StageSettings create(ChampionshipEventStageSettingsTo settings) {
        return new StageSettings(
                settings.routeID(),
                settings.route(),
                settings.weatherAndSurfaceID(),
                settings.weatherAndSurface(),
                settings.timeOfDayID(),
                settings.timeOfDay(),
                settings.serviceAreaID(),
                settings.serviceArea()
        );
    }

    private ZonedDateTime parseTimestamp(String timestamp) {
        var normalized = timestamp.charAt(timestamp.length() -1) == 'Z' ? timestamp : timestamp + "Z";
        return ZonedDateTime.parse(normalized, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }


    public ChampionshipStanding createStanding(ClubStandingsResultEntryTo entry) {
        return new ChampionshipStanding(
                UUID.randomUUID(),
                entry.ssid(),
                entry.displayName(),
                entry.pointsAccumulated(),
                entry.rank(),
                entry.nationalityID()
        );
    }
}
