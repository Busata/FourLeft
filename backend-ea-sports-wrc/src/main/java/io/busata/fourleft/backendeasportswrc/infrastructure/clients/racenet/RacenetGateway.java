package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.EAWRCToken;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.RacenetAuthorizationApi;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.EAWRCOfficialClubsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetGateway {

    private final RacenetAuthorizationApi authorizationApi;
    private final EASportsWRCRacenetApi api;

    public EAWRCOfficialClubsTo getClubs() {
        return doAuthorizedCall((headers) -> api.getClubs(headers));
    }

    public CompletableFuture<ClubDetailsTo> getClubDetail(String clubId) {
        return doAuthorizedCall((headers -> api.getClubDetails(headers, clubId)));
    }

    public CompletableFuture<List<String>> getLeaderboard(String leaderboardId) {
        return null;
    }

    private <T> T doAuthorizedCall(Function<HttpHeaders, T> supplier) {
        return supplier.apply(getHeaders());
    }


    @Cacheable("authorization_headers")
    public HttpHeaders getHeaders() {
        EAWRCToken rawHeaders = authorizationApi.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + rawHeaders.accessToken());
        headers.set("X-Bot", "fourleft.io");
        headers.set("X-Contact", "driesdesmet@gmail.com");

        return headers;
    }
}
