package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial;

import feign.FeignException;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.EAWRCToken;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.RacenetAuthorizationApi;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.EASportsWRCRacenetApi;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial.TimeTrialLeaderboardResultTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Real Racenet-backed {@link TimeTrialBoardGateway}, hitting
 * {@code GET /api/wrc2023Stats/leaderboard/{routeId}/{vehicleClassId}/{surface}} on the same host and
 * with the same auth as the club calls ({@code EASportsWRCRacenetApi} / {@code web-api.racenet.com}).
 *
 * <p>The probe only reads {@code totalEntrantCount}, so it requests a single result. Existence:
 * a 2xx means the board is there (entry count may be 0); a 404 means there is no board for this
 * combination — a valid outcome, not an error. Any other failure (auth, rate-limit, 5xx) propagates
 * so the probe pass surfaces it rather than recording a bogus "missing".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetTimeTrialBoardGateway implements TimeTrialBoardGateway {

    /** The probe only needs the total count from the envelope, so fetch the smallest page. */
    private static final int PROBE_RESULT_COUNT = 1;

    private final RacenetAuthorizationApi authorizationApi;
    private final EASportsWRCRacenetApi api;

    @Override
    public ProbeResult probe(long locationId, long routeId, int surfaceCondition, long vehicleClassId) {
        try {
            TimeTrialLeaderboardResultTo result = api.getTimeTrialLeaderboard(
                    getHeaders(), routeId, vehicleClassId, surfaceCondition, PROBE_RESULT_COUNT, false, 0);

            long total = (result == null || result.totalEntrantCount() == null) ? 0L : result.totalEntrantCount();
            return ProbeResult.exists((int) total);
        } catch (FeignException.NotFound ex) {
            return ProbeResult.missing();
        }
    }

    private HttpHeaders getHeaders() {
        EAWRCToken token = authorizationApi.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.accessToken());
        headers.set("X-Contact", "mail: driesdesmet@gmail.com; discord: busata");
        headers.set("X-Message", "It was PJ's idea all along!");

        return headers;
    }
}
