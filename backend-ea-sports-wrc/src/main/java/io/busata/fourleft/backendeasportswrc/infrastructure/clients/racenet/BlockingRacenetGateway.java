package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.EAWRCToken;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.RacenetAuthorizationApi;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardParamsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubStandingsParamsTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Blocking counterpart to {@link RacenetGateway}. Same Racenet calls, but each method returns the
 * result directly and blocks the caller instead of handing back a {@code CompletableFuture}.
 *
 * <p>Intended for the virtual-thread importer ({@code application.importer.vt.*}), where each club
 * runs on its own virtual thread and blocking a call is cheap. The underlying Feign client is
 * synchronous anyway, so this just drops the {@code supplyAsync} wrapper the async gateway adds.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BlockingRacenetGateway {

    private final RacenetAuthorizationApi authorizationApi;
    private final EASportsWRCRacenetApi api;

    public ClubDetailsTo getClubDetail(String clubId) {
        return api.getClubDetails(getHeaders(), clubId);
    }

    public ChampionshipTo getChampionship(String championshipId) {
        return api.getChampionship(getHeaders(), championshipId);
    }

    public ClubLeaderboardResultTo getLeaderboard(String clubId, String leaderboardId, ClubLeaderboardParamsTo params) {
        return api.getClubLeaderboard(getHeaders(), clubId, leaderboardId, params.maxResultCount(), params.platform(), params.cursor());
    }

    public ClubStandingsResultTo getStandings(String clubId, String championshipId, ClubStandingsParamsTo params) {
        return api.getClubStandings(getHeaders(), clubId, championshipId, params.resultCount(), params.cursor());
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
