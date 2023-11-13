package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.EAWRCToken;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.RacenetAuthorizationApi;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardParamsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubStandingsParamsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetGateway {

    private final RacenetAuthorizationApi authorizationApi;
    private final EASportsWRCRacenetApi api;

    public CompletableFuture<ClubDetailsTo> getClubDetail(String clubId) {
        return doAuthorizedCall((headers -> {
            return CompletableFuture.supplyAsync(() -> api.getClubDetails(headers, clubId));
        }));
    }
    public CompletableFuture<ChampionshipTo> getChampionship(String championshipId) {
        return doAuthorizedCall((headers -> {
            return CompletableFuture.supplyAsync(() -> api.getChampionship(headers, championshipId));
        }));
    }

    public CompletableFuture<ClubLeaderboardResultTo> getLeaderboard(String clubId, String leaderboardId, ClubLeaderboardParamsTo params) {
        return doAuthorizedCall((headers -> {
            return CompletableFuture.supplyAsync(() -> api.getClubLeaderboard(headers, clubId, leaderboardId, params.maxResultCount(), params.platform(), params.cursor()));
        }));
    }

    public CompletableFuture<ClubStandingsResultTo> getStandings(String clubId, String championshipId, ClubStandingsParamsTo params) {
        return doAuthorizedCall((headers) -> {
            return CompletableFuture.supplyAsync(() -> api.getClubStandings(headers, clubId, championshipId, params.resultCount(), params.cursor()));

        });
    }

    private <T> T doAuthorizedCall(Function<HttpHeaders, T> supplier) {
        return supplier.apply(getHeaders());
    }


    public HttpHeaders getHeaders() {
        EAWRCToken rawHeaders = authorizationApi.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + rawHeaders.accessToken());
        headers.set("X-Contact", "mail: driesdesmet@gmail.com; discord: busata");
        headers.set("X-Message", "It was PJ's idea all along!");

        return headers;
    }
}
