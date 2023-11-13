package io.busata.fourleft.backendeasportswrc.test.usecase;


import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.*;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipEventSettingsTo.ChampionshipEventSettingsToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipEventStageSettingsTo.ChampionshipEventStageSettingsToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipEventStageTo.ChampionshipEventStageToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipEventTo.ChampionshipEventToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipSettingsTo.ChampionshipSettingsToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo.ChampionshipToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo.ClubDetailsToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardEntryTo.ClubLeaderboardEntryToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardResultTo.ClubLeaderboardResultToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultEntryTo.ClubStandingsResultEntryToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultTo.ClubStandingsResultToBuilder;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class RacenetApiFixtures {

    public static Supplier<ClubDetailsToBuilder> clubDetailsFixture = () -> ClubDetailsTo.builder()
            .clubID("11")
            .officialClubType("1")
            .status(1L)
            .role(3L)
            .reaction(0L)
            .creationSSID("1015304901174")
            .creatorDisplayName("EA SPORTS WRC")
            .ownerDisplayName("EA SPORTS WRC")
            .ownerProfileImageUrl("")
            .clubName("Official WRC")
            .clubDescription("The official club for EA SPORTS WRC! This Club features events using WRC, WRC2 and Junior WRC car classes.")
            .activeMemberCount(5L)
            .likeCount(5L)
            .dislikeCount(5L)
            .imageCatalogueID("")
            .platform(0L)
            .accessLevel(0L)
            .clubCreatedAt("2023-10-23T13:53:41.7032986")
            .socialMediaLinks(List.of())
            .championshipIDs(List.of())
            .currentChampionship(Optional.empty());

    public static Supplier<ChampionshipSettingsToBuilder> championshipSettingsToFixture = () -> ChampionshipSettingsTo.builder()
            .name("Championship Name")
            .format(1L)
            .bonusPointsMode(0L)
            .scoringSystem(0L)
            .trackDegradation(0L)
            .isHardcoreDamageEnabled(true)
            .isAssistsAllowed(true)
            .isTuningAllowed(true);

    public static Supplier<ChampionshipToBuilder> championshipFixture = () -> ChampionshipTo.builder()
            .id("championshipId-1")
            .clubId("11")
            .absoluteOpenDate(ApplicationClock.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .absoluteCloseDate(ApplicationClock.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME))
            .leaderboardID("leaderboardId-1")
            .settings(championshipSettingsToFixture.get().build())
            .events(List.of());


    public static Supplier<ChampionshipEventSettingsToBuilder> eventSettingsFixture = () -> ChampionshipEventSettingsTo.builder()
            .vehicleClassID(0L)
            .vehicleClass("")
            .weatherSeasonID(0L)
            .weatherSeason("")
            .locationID(0L)
            .location("")
            .duration("8.00:00:00");

    public static Supplier<ChampionshipEventToBuilder> championshipEventFixture = () -> ChampionshipEventTo.builder()
            .id("event-1")
            .leaderboardID("leaderboardId-1")
            .absoluteOpenDate(ApplicationClock.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .absoluteCloseDate(ApplicationClock.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME))
            .status(1L)
            .eventSettings(eventSettingsFixture.get().build())
            .stages(List.of());

    public static Supplier<ChampionshipEventStageSettingsToBuilder> stageSettingsFixture = () -> ChampionshipEventStageSettingsTo.builder()
            .routeID(0L)
            .route("")
            .weatherAndSurfaceID(0L)
            .weatherAndSurface("")
            .timeOfDayID(0L)
            .timeOfDay("")
            .serviceAreaID(0L)
            .serviceArea("");

    public static Supplier<ChampionshipEventStageToBuilder> stageFixture = () -> ChampionshipEventStageTo.builder()
            .id("stageid")
            .leaderboardID("stageLeaderboardId")
            .stageSettings(stageSettingsFixture.get().build());


    public static Supplier<ClubLeaderboardResultToBuilder> leaderboardFixture = () -> ClubLeaderboardResultTo.builder()
            .next(null)
            .previous(null)
            .totalEntrantCount(0L)
            .entries(List.of());

    public static Supplier<ClubLeaderboardEntryToBuilder> entryFixture = () -> ClubLeaderboardEntryTo.builder()
            .ssid("1")
            .displayName("WRC Driver")
            .rank(1L)
            .leaderboardId("")
            .wrcPlayerId("")
            .time("00:07:12.490")
            .differenceToFirst("0:00:0")
            .nationalityID(1L)
            .timeAccumulated("00:07:12.490")
            .platform(0L)
            .vehicle("Alpine A110")
            .timePenalty("0:00:0");

    public static Supplier<ClubStandingsResultToBuilder> clubStandingsFixture = () -> ClubStandingsResultTo.builder()
            .cursorNext(null)
            .cursorPrevious(null)
            .entries(List.of());

    public static Supplier<ClubStandingsResultEntryToBuilder> clubStandingsEntryFixture = () -> ClubStandingsResultEntryTo.builder()
            .ssid("1234")
            .pointsAccumulated(10)
            .rank(1)
            .displayName("WRC Driver")
            .nationalityID(1);

}
