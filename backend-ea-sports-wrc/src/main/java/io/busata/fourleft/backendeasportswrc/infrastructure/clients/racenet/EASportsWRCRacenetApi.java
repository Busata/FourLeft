package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultTo;
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
}
