package io.busata.fourleft.backendeasportswrc.test.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultTo;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@Component
public class RacenetApiWireMocks {

    @Autowired
    private ObjectMapper mapper;
    

    @SneakyThrows
    public void createClubMocks(WireMockServer racenetApi, ClubDetailsTo details) {

           racenetApi.stubFor(get("/api/wrc2023clubs/%s?includeChampionship=true".formatted(details.clubID()))
                    .willReturn(aResponse()
                            .withHeader("Content-Type","application/json")
                            .withBody(this.mapper.writeValueAsString(details))
            ));
    }

    @SneakyThrows
    public void createClubDetailsFailure(WireMockServer racenetApi, ClubDetailsTo details) {

            racenetApi.stubFor(get("/api/wrc2023clubs/%s?includeChampionship=true".formatted(details.clubID()))
                    .willReturn(aResponse()
                            .withHeader("Content-Type","application/json")
                            .withStatus(500)
            ));
    }

    @SneakyThrows
    public void createLeaderboards(WireMockServer racenetApi, String clubId, String leaderboardId, ClubLeaderboardResultTo leaderboardResult) {
            racenetApi.stubFor(get(urlPathMatching("/api/wrc2023clubs/%s/leaderboard/%s".formatted(clubId, leaderboardId)))
                    .withQueryParam("MaxResultCount", equalTo("10"))
                    .withQueryParam("Platform", equalTo("0"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type","application/json")
                            .withBody(this.mapper.writeValueAsString(leaderboardResult))
                    ));
    }
    @SneakyThrows
    public void createStandings(WireMockServer racenetApi, String clubId, String championshipId, ClubStandingsResultTo standingsResult) {
            racenetApi.stubFor(get(urlPathMatching("/api/wrc2023clubs/%s/championship/points/%s".formatted(clubId, championshipId)))
                    .withQueryParam("ResultCount", equalTo("10"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type","application/json")
                            .withBody(this.mapper.writeValueAsString(standingsResult))
                    ));
    }

    @SneakyThrows
    public void createLeaderboardsPage(WireMockServer racenetApi, String clubId, String leaderboardId, String cursor, ClubLeaderboardResultTo leaderboardResult) {
            racenetApi.stubFor(get(urlPathMatching("/api/wrc2023clubs/%s/leaderboard/%s".formatted(clubId, leaderboardId)))
                            .withQueryParam("MaxResultCount", equalTo("10"))
                            .withQueryParam("Platform", equalTo("0"))
                            .withQueryParam("Cursor", equalTo(cursor))

                    .willReturn(aResponse()
                            .withHeader("Content-Type","application/json")
                            .withBody(this.mapper.writeValueAsString(leaderboardResult))
                    ));
    }

    @SneakyThrows
    public void createLeaderboardsPageFailure(WireMockServer racenetApi, String clubId, String leaderboardId, String cursor) {
            racenetApi.stubFor(get(urlPathMatching("/api/wrc2023clubs/%s/leaderboard/%s".formatted(clubId, leaderboardId)))
                            .withQueryParam("MaxResultCount", equalTo("10"))
                            .withQueryParam("Platform", equalTo("0"))
                            .withQueryParam("Cursor", equalTo(cursor))

                    .willReturn(aResponse()
                            .withHeader("Content-Type","application/json")
                            .withStatus(500)
                    ));
    }

}
