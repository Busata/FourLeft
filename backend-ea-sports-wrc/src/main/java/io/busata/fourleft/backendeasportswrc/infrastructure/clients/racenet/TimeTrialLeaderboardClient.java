package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial.TimeTrialLeaderboardResultTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Racenet public time-trial leaderboard API, kept as its own Feign client so the resilience4j
 * {@code @RateLimiter}/{@code @Retry} (instance {@code racenet-timetrial}, configured in
 * {@code application.yml}) throttle the probe/fetch sweep <em>without</em> affecting the club calls
 * on {@link EASportsWRCRacenetApi}. Same host + auth headers as the club API.
 */
@FeignClient(name = "racenet-timetrial", url = "${racenet-api.url}")
@RateLimiter(name = "racenet-timetrial")
@Retry(name = "racenet-timetrial")
public interface TimeTrialLeaderboardClient {

    /**
     * Public time-trial leaderboard for a stage. Path is {@code routeId/vehicleClassId/surface}
     * (surface 0=dry, 1=wet) — location is implied by the route. Responds 404 when no board exists.
     *
     * <p>{@code cursor} pages the board: pass {@code null} for the first page, then feed back the
     * response's {@code next} until it comes back blank (same cursor scheme as the club leaderboard).
     * The probe leaves it null and asks for a single result.
     */
    @GetMapping("/api/wrc2023Stats/leaderboard/{routeId}/{vehicleClassId}/{surfaceCondition}")
    TimeTrialLeaderboardResultTo getTimeTrialLeaderboard(@RequestHeader HttpHeaders headers,
                                                         @PathVariable long routeId,
                                                         @PathVariable long vehicleClassId,
                                                         @PathVariable int surfaceCondition,
                                                         @RequestParam(name = "maxResultCount") int maxResultCount,
                                                         @RequestParam(name = "focusOnMe") boolean focusOnMe,
                                                         @RequestParam(name = "platform") int platform,
                                                         @RequestParam(name = "cursor", required = false) String cursor
    );
}
