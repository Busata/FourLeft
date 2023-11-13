package io.busata.fourleft.backendeasportswrc.application.discord.autoposting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import io.busata.fourleft.backendeasportswrc.application.importer.ClubsImporterService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.EAWRCToken;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import io.busata.fourleft.backendeasportswrc.test.AbstractIntegrationTest;
import io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiWireMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiFixtures.*;
import static io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiFixtures.entryFixture;

@EnableWireMock({
        @ConfigureWireMock(name = "racenet-api", property = "racenet-api.url"),
        @ConfigureWireMock(name = "authorization-api", property = "racenetauthentication.url")
})
class DiscordAutoPostingServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @InjectWireMock("racenet-api")
    private WireMockServer racenetApi;

    @InjectWireMock("authorization-api")
    private WireMockServer authorizationApi;

    @Autowired
    private ClubsImporterService clubsImporterService;

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
    @DataSet(provider = AutopostDataSet.class, cleanBefore = true, skipCleaningFor = "schema_version")
    public void testAutoposting() throws InterruptedException {
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
                .next(null)
                .totalEntrantCount(5L)
                .entries(List.of(
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 1").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 2").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 3").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 4").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 5").build()
                ))
                .build());

        runSyncUntilDone();
        runSyncUntilDone();
        Thread.sleep(100);

        setClock(ApplicationClock.now().plusHours(1));

        racenetApiWireMocks.createLeaderboards(racenetApi, "11", "eventBoard", leaderboardFixture.get()
                .next(null)
                .totalEntrantCount(5L)
                .entries(List.of(
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 1").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 2").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 3").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 4").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 5").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 6").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 7").build(),
                        entryFixture.get().leaderboardId("eventBoard").displayName("Eventer 8").build()
                ))
                .build());

        runSyncUntilDone();

    }


    private void runSyncUntilDone() throws InterruptedException {
        int runningProcesses;
        do {
            runningProcesses = clubsImporterService.sync();
            Thread.sleep(100);
        } while (runningProcesses != 0);
    }

}