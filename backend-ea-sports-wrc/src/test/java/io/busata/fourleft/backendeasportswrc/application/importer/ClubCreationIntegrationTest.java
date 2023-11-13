package io.busata.fourleft.backendeasportswrc.application.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiFixtures.championshipFixture;
import static io.busata.fourleft.backendeasportswrc.test.usecase.RacenetApiFixtures.clubDetailsFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnableWireMock({
        @ConfigureWireMock(name = "racenet-api", property = "racenet-api.url"),
        @ConfigureWireMock(name = "authorization-api", property = "racenetauthentication.url")
})
class ClubCreationIntegrationTest extends AbstractIntegrationTest {

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
    public void testClubCreation() throws InterruptedException {

        racenetApiWireMocks.createClubMocks(racenetApi,
                clubDetailsFixture.get()
                        .clubID("11")
                        .clubName("Official WRC")
                        .currentChampionship(Optional.of(
                                championshipFixture.get()
                                        .clubId("11")
                                        .build()
                        ))
                        .build());

        //Creates club
        runSyncUntilDone();

        Club clubCreated = clubService.findById("11");

        assertThat(clubCreated).isNotNull();
        assertThat(clubCreated.getId()).isEqualTo("11");
        assertThat(clubCreated.getClubName()).isEqualTo("Official WRC");

        List<Championship> championshipsByClubId = championshipService.findChampionshipsByClubId("11");

        assertThat(championshipsByClubId).hasSize(1);
    }

    @Test
    @DataSet(provider = BaseDataSet.class, cleanBefore = true, skipCleaningFor = "schema_version")
    public void testClubCreationFailure() throws InterruptedException {

        racenetApiWireMocks.createClubDetailsFailure(racenetApi, clubDetailsFixture.get().clubID("11").build());


        assertThat(clubConfigurationService.findSyncableClubs()).isNotEmpty();

        runSyncUntilDone();

        assertThrows(NoSuchElementException.class, () -> clubService.findById("11"));

        assertThat(clubConfigurationService.findSyncableClubs()).isEmpty();


    }


    private void runSyncUntilDone() throws InterruptedException {
        int runningProcesses;
        do {
            runningProcesses = clubsImporterService.sync();
            Thread.sleep(100);
        } while (runningProcesses != 0);
    }
}