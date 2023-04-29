package io.busata.fourleft.infrastructure.clients.racenet;



import io.busata.fourleft.infrastructure.clients.racenet.dto.communityevents.DR2CommunityEvent;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubChampionships;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubDetails;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubMembers;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubRecentResults;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreateRequestTo;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreationStatus;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipDeleteStatus;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipOptions;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.standings.DR2ChampionshipStandings;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardRequest;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardResults;
import io.busata.fourleft.infrastructure.clients.racenet.dto.security.DR2InitialState;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name="racenetapi", url="https://dirtrally2.dirtgame.com/", configuration = FeignConfiguration.class)
interface RacenetApi {
    @GetMapping(value = "api/ClientStore/GetInitialState", produces = MediaType.APPLICATION_JSON_VALUE)
    DR2InitialState getInitialState(@RequestHeader HttpHeaders headers);

    @GetMapping("api/Club/{clubId}")
    DR2ClubDetails getClubDetails(@RequestHeader HttpHeaders headers, @PathVariable("clubId") long clubId);

    @GetMapping("api/Club/{clubId}/championships")
    List<DR2ClubChampionships> getClubChampionships(@RequestHeader HttpHeaders headers, @PathVariable("clubId") long clubId);

    @GetMapping("api/Club/{clubId}/recentResults")
    DR2ClubRecentResults getClubRecentResults(@RequestHeader HttpHeaders headers, @PathVariable("clubId") long clubId);

    @PostMapping("api/Leaderboard")
    DR2LeaderboardResults getLeaderboard(@RequestHeader HttpHeaders headers, @RequestBody DR2LeaderboardRequest request);

    @GetMapping("api/Club/championship/options")
    DR2ChampionshipOptions getOptions(@RequestHeader HttpHeaders headers);

    @PostMapping("api/Club/{clubId}/championship")
    DR2ChampionshipCreationStatus createChampionship(@RequestHeader HttpHeaders headers, @PathVariable("clubId") long clubId, @RequestBody DR2ChampionshipCreateRequestTo championshipCreateTo);

    @DeleteMapping("api/Club/{clubId}/activeChampionship")
    DR2ChampionshipDeleteStatus deleteActiveChampionship(@RequestHeader HttpHeaders headers, @PathVariable("clubId") long clubId);

    @GetMapping("api/Club/{clubId}/standings/current")
    DR2ChampionshipStandings getStandings(@RequestHeader HttpHeaders headers,
                                          @PathVariable("clubId") long clubId,
                                          @RequestParam long page,
                                          @RequestParam long pageLength
                                          );

    @GetMapping("api/Challenge/Community")
    List<DR2CommunityEvent> getCommunity(@RequestHeader HttpHeaders headers);

    @GetMapping("api/Club/{clubId}/members")
    DR2ClubMembers getMembers(@RequestHeader HttpHeaders headers, @PathVariable("clubId") long clubId, @RequestParam int pageSize, @RequestParam int page);
}
