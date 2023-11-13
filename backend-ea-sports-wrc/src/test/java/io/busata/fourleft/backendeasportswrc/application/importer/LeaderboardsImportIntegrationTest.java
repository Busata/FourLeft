package io.busata.fourleft.backendeasportswrc.application.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboard;
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

import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnableWireMock({
        @ConfigureWireMock(name = "racenet-api", property = "racenet-api.url"),
        @ConfigureWireMock(name = "authorization-api", property = "racenetauthentication.url")
})
class LeaderboardsImportIntegrationTest extends AbstractIntegrationTest {

    @InjectWireMock("authorization-api")
    private WireMockServer authorizationApi;

    @InjectWireMock("racenet-api")
    private WireMockServer racenetApi;

    @Autowired
    private ClubsImporterService clubsImporterService;

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
    public void testLeaderboardPagesImport() throws InterruptedException {
        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .clubName("Official WRC")
                        .currentChampionship(Optional.of(
                                championshipFixture.get()
                                        .clubId("11")
                                        .events(List.of(
                                                championshipEventFixture.get()
                                                        .leaderboardID("eventBoard")
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build());


        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "eventBoard", leaderboardFixture.get()
                .next("cursorPage2")
                .totalEntrantCount(5L)
                .entries(List.of(
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 1").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 2").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 3").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 4").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 5").build()
                ))
                .build());

        racenetApiWireMocks.createLeaderboardsPage(racenetApi, "11", "eventBoard", "cursorPage2", leaderboardFixture.get()
                .next("cursorPage3")
                .totalEntrantCount(5L)
                .entries(List.of(
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 6").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 7").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 8").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 9").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 10").build()
                ))
                .build());

        racenetApiWireMocks.createLeaderboardsPage(racenetApi, "11", "eventBoard", "cursorPage3", leaderboardFixture.get()
                .next(null)
                .totalEntrantCount(5L)
                .entries(List.of(
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 11").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 12").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 13").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 14").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 15").build()
                ))
                .build());

        runSyncUntilDone();
        runSyncUntilDone();

        ClubLeaderboard eventBoard = clubLeaderboardService.findById("eventBoard");
        assertThat(eventBoard.getTotalEntries()).isEqualTo(15L);
    }

    @Test
    @DataSet(provider = BaseDataSet.class, cleanBefore = true, skipCleaningFor = "schema_version")
    public void testLeaderboardPagesImportFailureOnPage() throws InterruptedException {
        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .clubName("Official WRC")
                        .currentChampionship(Optional.of(
                                championshipFixture.get()
                                        .clubId("11")
                                        .events(List.of(
                                                championshipEventFixture.get()
                                                        .leaderboardID("eventBoard")
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build());


        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "eventBoard", leaderboardFixture.get()
                .next("cursorPage2")
                .totalEntrantCount(5L)
                .entries(List.of(
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 1").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 2").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 3").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 4").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 5").build()
                ))
                .build());

        racenetApiWireMocks.createLeaderboardsPageFailure(racenetApi, "11", "eventBoard", "cursorPage2");

        runSyncUntilDone();
        runSyncUntilDone();

        assertThrows(NoSuchElementException.class, () -> clubLeaderboardService.findById("eventBoard"));
    }

    @Test
    @DataSet(provider = BaseDataSet.class, cleanBefore = true, skipCleaningFor = "schema_version")
    public void testLeaderboardImport() throws InterruptedException {
        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .clubName("Official WRC")
                        .currentChampionship(Optional.of(
                                championshipFixture.get()
                                        .clubId("11")
                                        .events(List.of(
                                                championshipEventFixture.get()
                                                        .leaderboardID("eventBoard")
                                                        .stages(List.of(
                                                                stageFixture.get()
                                                                        .leaderboardID("stageBoard")
                                                                        .build()
                                                        ))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build());

        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "eventBoard", leaderboardFixture.get()
                .totalEntrantCount(1L)
                .entries(List.of(
                        entryFixture.get()
                                .leaderboardId("eventBoard")
                                .displayName("Eventer")
                                .build()
                ))
                .build());

        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "stageBoard", leaderboardFixture.get()
                .totalEntrantCount(1L)
                .entries(List.of(
                        entryFixture.get()
                                .leaderboardId("stageBoard")
                                .displayName("Stager")
                                .build(),
                        entryFixture.get()
                                .leaderboardId("stageBoard")
                                .displayName("Stager 2")
                                .build()
                ))
                .build());

        //Once to start, create and start the fetching asynchronously
        runSyncUntilDone();

        //Gets the leaderboard
        runSyncUntilDone();

        racenetApi.verify(exactly(2), getRequestedFor(urlPathMatching(".*/leaderboard/.*")));

        ClubLeaderboard eventBoard = clubLeaderboardService.findById("eventBoard");
        assertThat(eventBoard.getTotalEntries()).isEqualTo(1L);

        ClubLeaderboard stageBoard = clubLeaderboardService.findById("stageBoard");
        assertThat(stageBoard.getTotalEntries()).isEqualTo(2L);

        runSyncUntilDone();

        racenetApi.verify(exactly(2), getRequestedFor(urlPathMatching(".*/leaderboard/.*")));



    }

    private void runSyncUntilDone() throws InterruptedException {
        int runningProcesses;
        do {
            runningProcesses = clubsImporterService.sync();
            Thread.sleep(100);
        } while (runningProcesses != 0);
    }
}