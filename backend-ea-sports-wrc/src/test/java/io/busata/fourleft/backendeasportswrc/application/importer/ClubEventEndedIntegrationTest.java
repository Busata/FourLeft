package io.busata.fourleft.backendeasportswrc.application.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
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
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

@EnableWireMock({
        @ConfigureWireMock(name = "racenet-api", property = "racenet-api.url"),
        @ConfigureWireMock(name = "authorization-api", property = "racenetauthentication.url")
})
class ClubEventEndedIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected ObjectMapper mapper;

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
    public void testClubUpdate() throws InterruptedException {
        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .clubName("Official WRC")
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

        //Once to start, create and start the fetching asynchronously
        runSyncUntilDone();

        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .clubName("Official WRC")
                        .currentChampionship(Optional.of(
                                championshipFixture.get()
                                        .id("2")
                                        .clubId("11")
                                        .settings(championshipSettingsToFixture.get()
                                                .name("WRC Update")
                                                .build())
                                        .build()
                        ))
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

        setClock(ApplicationClock.now().plusDays(2));

        runSyncUntilDone();

        List<Championship> championshipsByClubId = championshipService.findChampionshipsByClubId("11");

        ClubLeaderboard eventBoard = clubLeaderboardService.findById("eventBoard");
        assertThat(eventBoard.getTotalEntries()).isEqualTo(3L);

        List<ChampionshipStanding> standings = championshipService.findStandings("1");
        assertThat(standings).hasSize(2);

        assertThat(championshipsByClubId).hasSize(2);
    }


    private void runSyncUntilDone() throws InterruptedException {
        int runningProcesses;
        do {
            runningProcesses = clubsImporterService.sync();
            Thread.sleep(100);
        } while (runningProcesses != 0);
    }
}