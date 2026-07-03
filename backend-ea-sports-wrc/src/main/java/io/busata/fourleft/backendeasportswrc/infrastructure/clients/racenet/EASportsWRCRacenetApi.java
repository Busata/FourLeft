package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial.TimeTrialLeaderboardResultTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="easportswrcapi", url="${racenet-api.url}")
public interface EASportsWRCRacenetApi {


    @GetMapping("/api/wrc2023clubs/{clubId}?includeChampionship=true")
    ClubDetailsTo getClubDetails(@RequestHeader HttpHeaders headers, @PathVariable String clubId);

    @GetMapping("/api/wrc2023clubs/championships/{championshipId}")
    ChampionshipTo getChampionship(@RequestHeader HttpHeaders headers, @PathVariable @RequestParam String championshipId);


    @GetMapping("/api/wrc2023clubs/{clubId}/leaderboard/{leaderboardId}")
    ClubLeaderboardResultTo getClubLeaderboard(@RequestHeader HttpHeaders headers,
                                               @PathVariable String clubId,
                                               @PathVariable String leaderboardId,
                                               @RequestParam(name="MaxResultCount") int maxResultCount,
                                               @RequestParam(name="Platform") int platform,
                                               @RequestParam(name="Cursor", required = false) String cursor
    );

    @GetMapping("/api/wrc2023clubs/{clubId}/championship/points/{championshipId}")
    ClubStandingsResultTo getClubStandings(@RequestHeader HttpHeaders headers,
                                           @PathVariable String clubId,
                                           @PathVariable String championshipId,
                                           @RequestParam(name="ResultCount") int resultCount,
                                           @RequestParam(name="Cursor", required = false) String cursor
    );

    /**
     * Public time-trial leaderboard for a stage. Path is {@code routeId/vehicleClassId/surface}
     * (surface 0=dry, 1=wet) — location is implied by the route. Responds 404 when no board exists.
     */
    @GetMapping("/api/wrc2023Stats/leaderboard/{routeId}/{vehicleClassId}/{surfaceCondition}")
    TimeTrialLeaderboardResultTo getTimeTrialLeaderboard(@RequestHeader HttpHeaders headers,
                                                         @PathVariable long routeId,
                                                         @PathVariable long vehicleClassId,
                                                         @PathVariable int surfaceCondition,
                                                         @RequestParam(name="maxResultCount") int maxResultCount,
                                                         @RequestParam(name="focusOnMe") boolean focusOnMe,
                                                         @RequestParam(name="platform") int platform
    );
}
