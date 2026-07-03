package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial;

import feign.FeignException;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.EAWRCToken;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization.RacenetAuthorizationApi;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.TimeTrialLeaderboardClient;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial.TimeTrialLeaderboardEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial.TimeTrialLeaderboardResultTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Real Racenet-backed {@link TimeTrialBoardGateway}, hitting
 * {@code GET /api/wrc2023Stats/leaderboard/{routeId}/{vehicleClassId}/{surface}} via
 * {@link TimeTrialLeaderboardClient} (rate-limited + retried by resilience4j), same host + auth as
 * the club calls.
 *
 * <p>Existence: a 2xx means the board is there (entry count may be 0); a 404 means there is no board
 * for this combination — a valid outcome, not an error. Any other failure (auth, rate-limit, 5xx)
 * propagates so the pass surfaces it rather than recording a bogus "missing". Each call to the
 * underlying Feign client is what the {@code racenet-timetrial} rate limiter meters, so a full fetch
 * of an N-entry board costs {@code ceil(N / FETCH_PAGE_SIZE)} permits. {@link #fetch} hands each page
 * straight to the caller and keeps no running list, so board size drives request count, not memory.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetTimeTrialBoardGateway implements TimeTrialBoardGateway {

    /** The probe only needs the total count from the envelope, so fetch the smallest page. */
    private static final int PROBE_RESULT_COUNT = 1;

    /**
     * Page size when fetching a full board. Racenet caps {@code maxResultCount} at 20 for this
     * endpoint (larger values are clamped to 20), so this is the most rows a page can carry — a big
     * board is unavoidably many calls. The global {@code racenet-timetrial} rate limiter, not the page
     * size, governs how gentle the sweep is.
     */
    private static final int FETCH_PAGE_SIZE = 20;

    private final RacenetAuthorizationApi authorizationApi;
    private final TimeTrialLeaderboardClient client;

    @Override
    public ProbeResult probe(long locationId, long routeId, int surfaceCondition, long vehicleClassId) {
        try {
            TimeTrialLeaderboardResultTo result = client.getTimeTrialLeaderboard(
                    getHeaders(), routeId, vehicleClassId, surfaceCondition, PROBE_RESULT_COUNT, false, 0, null);
            return ProbeResult.exists(totalOf(result));
        } catch (FeignException.NotFound ex) {
            return ProbeResult.missing();
        }
    }

    @Override
    public FetchResult fetch(long locationId, long routeId, int surfaceCondition, long vehicleClassId,
                             Consumer<List<TimeTrialLeaderboardEntryTo>> pageConsumer) {
        HttpHeaders headers = getHeaders();

        TimeTrialLeaderboardResultTo page;
        try {
            page = client.getTimeTrialLeaderboard(
                    headers, routeId, vehicleClassId, surfaceCondition, FETCH_PAGE_SIZE, false, 0, null);
        } catch (FeignException.NotFound ex) {
            return FetchResult.missing();
        }

        int total = totalOf(page);

        // Stream each non-empty page to the consumer, then follow the cursor. Stopping on an empty
        // page too (not just a blank cursor) guards against a board that keeps handing back a
        // non-blank `next` past its last row, which would otherwise loop forever mid-backfill.
        while (page != null && page.entries() != null && !page.entries().isEmpty()) {
            pageConsumer.accept(page.entries());
            String cursor = nextCursor(page);
            if (cursor == null) {
                break;
            }
            page = client.getTimeTrialLeaderboard(
                    headers, routeId, vehicleClassId, surfaceCondition, FETCH_PAGE_SIZE, false, 0, cursor);
        }

        return FetchResult.exists(total);
    }

    private static int totalOf(TimeTrialLeaderboardResultTo page) {
        return (page == null || page.totalEntrantCount() == null) ? 0 : page.totalEntrantCount().intValue();
    }

    private static String nextCursor(TimeTrialLeaderboardResultTo page) {
        if (page == null || page.next() == null || page.next().isBlank()) {
            return null;
        }
        return page.next();
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
