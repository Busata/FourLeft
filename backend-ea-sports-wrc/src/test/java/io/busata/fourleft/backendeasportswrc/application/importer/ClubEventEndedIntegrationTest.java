package io.busata.fourleft.backendeasportswrc.application.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import io.busata.fourleft.api.easportswrc.events.ClubEventEnded;
import io.busata.fourleft.backendeasportswrc.application.importer.vt.ClubImportWorker;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboard;
import io.busata.fourleft.backendeasportswrc.domain.models.EventStatus;
import io.busata.fourleft.backendeasportswrc.domain.services.championships.ChampionshipService;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.EAWRCToken;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import io.busata.fourleft.backendeasportswrc.test.AbstractIntegrationTest;
import io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiWireMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

@EnableWireMock({
        @ConfigureWireMock(name = "racenet-api", property = "racenet-api.url"),
        @ConfigureWireMock(name = "authorization-api", property = "racenetauthentication.url")
})
@RecordApplicationEvents
class ClubEventEndedIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected ObjectMapper mapper;

    @InjectWireMock("authorization-api")
    private WireMockServer authorizationApi;

    @InjectWireMock("racenet-api")
    private WireMockServer racenetApi;

    @Autowired
    private ClubImportWorker clubImportWorker;

    @Autowired
    private ClubConfigurationService clubConfigurationService;

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubLeaderboardService clubLeaderboardService;

    @Autowired
    private ChampionshipService championshipService;

    @Autowired
    private RacenetApiWireMocks racenetApiWireMocks;

    @BeforeEach
    public void setupStubs() {
        ApplicationClock.CLOCK.set(Clock.systemDefaultZone());
        racenetApi.resetAll();

        String body;
        try {
            body = mapper.writeValueAsString(new EAWRCToken("dummy", "dummy", 1L, "dummy"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        authorizationApi.stubFor(get("/api/external/authentication/easportswrc").willReturn(
                        aResponse().withHeader("Content-Type", "application/json")
                                .withBody(body)
                )
        );
    }


    @Test
    @DataSet(provider = BaseDataSet.class, cleanBefore = true, skipCleaningFor = "schema_version")
    public void testClubUpdate() {
        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .clubName("Official WRC")
                        .championshipIDs(List.of("1"))
                        .currentChampionship(Optional.of(
                                championshipFixture.get()
                                        .id("1")
                                        .clubId("11")
                                        .settings(championshipSettingsToFixture.get()
                                                .name("WRC Creation")
                                                .build())

                                        .events(List.of(
                                                        championshipEventFixture.get()
                                                                .leaderboardID("eventBoard")
                                                                .build()
                                                )
                                        )
                                        .build()
                        ))
                        .build());
        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "eventBoard", leaderboardFixture.get().build());

        //Once to start, create and start the fetching asynchronously
        runImportUntilDone();

        // The finished championship stays listed; a fresh one has replaced it as current.
        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .clubName("Official WRC")
                        .championshipIDs(List.of("1", "2"))
                        .currentChampionship(Optional.of(
                                championshipFixture.get()
                                        .id("2")
                                        .clubId("11")
                                        .absoluteCloseDate(ApplicationClock.now().plusDays(30).format(DateTimeFormatter.ISO_DATE_TIME))
                                        .settings(championshipSettingsToFixture.get()
                                                .name("WRC Update")
                                                .build())
                                        .build()
                        ))
                        .build());
        racenetApiWireMocks.createChampionshipMocks(racenetApi,
                championshipFixture.get()
                        .id("1")
                        .clubId("11")
                        .build());

        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "eventBoard", leaderboardFixture.get()
                .totalEntrantCount(3L)
                .entries(List.of(
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 1").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 2").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 3").build()
                ))
                .build());

        racenetApiWireMocks.createStandings(racenetApi, "11", "1", clubStandingsFixture.get().entries(List.of(
                clubStandingsEntryFixture.get().ssid("1").displayName("Eventer 1").rank(1).pointsAccumulated(20).build(),
                clubStandingsEntryFixture.get().ssid("2").displayName("Eventer 2").rank(2).pointsAccumulated(10).build()
        )).build());
        racenetApiWireMocks.createStandings(racenetApi, "11", "2", clubStandingsFixture.get().entries(List.of()).build());

        setClock(ApplicationClock.now().plusDays(2));

        runImportUntilDone();

        List<Championship> championshipsByClubId = championshipService.findChampionshipsByClubId("11");

        ClubLeaderboard eventBoard = clubLeaderboardService.findById("eventBoard");
        assertThat(eventBoard.getTotalEntries()).isEqualTo(3L);

        List<ChampionshipStanding> standings = championshipService.findStandings("1");
        assertThat(standings).hasSize(2);

        assertThat(championshipsByClubId).hasSize(2);
    }

    /**
     * Daily-club rollover: the championship finishes together with its last event, and for a moment
     * the refreshed details expose no current championship at all. This used to blow up in
     * {@code eventEnded} (no active championship to pull standings from), which disabled the club's
     * sync and stalled the results post until the next {@code resetDisabledClubs} cron.
     */
    @Test
    @DataSet(provider = BaseDataSet.class, cleanBefore = true, skipCleaningFor = "schema_version")
    public void championshipEndingWithItsLastEventKeepsSyncAndAnnouncesOnce(ApplicationEvents applicationEvents) {
        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .championshipIDs(List.of("1"))
                        .currentChampionship(Optional.of(
                                championshipFixture.get()
                                        .id("1")
                                        .clubId("11")
                                        .events(List.of(
                                                championshipEventFixture.get()
                                                        .leaderboardID("eventBoard")
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build());
        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "eventBoard", leaderboardFixture.get().build());

        runImportUntilDone();

        // Rollover gap: the championship ended with its last event, no new one is active yet.
        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .championshipIDs(List.of("1"))
                        .currentChampionship(Optional.empty())
                        .build());
        racenetApiWireMocks.createChampionshipMocks(racenetApi,
                championshipFixture.get()
                        .id("1")
                        .clubId("11")
                        .build());
        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "eventBoard", leaderboardFixture.get()
                .totalEntrantCount(2L)
                .entries(List.of(
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 1").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 2").rank(2L).ssid("2").build()
                ))
                .build());
        racenetApiWireMocks.createStandings(racenetApi, "11", "1", clubStandingsFixture.get().entries(List.of(
                clubStandingsEntryFixture.get().ssid("1").displayName("Eventer 1").rank(1).pointsAccumulated(20).build()
        )).build());

        setClock(ApplicationClock.now().plusDays(2));

        runImportUntilDone();

        // The rollover no longer disables the club's sync…
        assertThat(clubConfigurationService.findSyncableClubs())
                .extracting(ClubConfiguration::getClubId)
                .contains("11");

        // …the history pass closed the championship out with the final board and standings…
        Championship championship = championshipService.findChampionshipsByClubId("11").get(0);
        assertThat(championship.getStatus()).isEqualTo(EventStatus.FINISHED);
        assertThat(championship.isUpdatedAfterFinish()).isTrue();
        assertThat(clubLeaderboardService.findById("eventBoard").getTotalEntries()).isEqualTo(2L);
        assertThat(championshipService.findStandings("1")).hasSize(1);

        // …and the ended event was announced exactly once (no post skipped, no double post).
        assertThat(applicationEvents.stream(ClubEventEnded.class)).hasSize(1);
    }


    /**
     * Drives {@link ClubImportWorker#importClub} directly until the club's domain state reports no
     * work left. The schedule/queue layer is deliberately bypassed: it re-submits clubs forever, so
     * "sync() returned 0" is not a usable completion signal in a test.
     */
    private void runImportUntilDone() {
        for (int i = 0; i < 10; i++) {
            if (!clubService.requiresImport("11")) {
                return;
            }
            clubImportWorker.importClub("11");
        }
        throw new AssertionError("Club 11 still requires import after 10 cycles");
    }
}
