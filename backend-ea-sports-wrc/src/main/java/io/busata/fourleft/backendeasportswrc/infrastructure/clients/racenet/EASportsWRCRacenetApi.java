package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.EAWRCOfficialClubsTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.concurrent.CompletableFuture;

@FeignClient(name="easportswrcapi", url="https://web-api.racenet.com/")
public interface EASportsWRCRacenetApi {

    @GetMapping("api/wrc2023clubs/official?includeChampionship=true")
    EAWRCOfficialClubsTo getClubs(@RequestHeader HttpHeaders headers);


    @GetMapping("api/wrc2023clubs/{clubId}?includeChampionship=true")
    CompletableFuture<ClubDetailsTo> getClubDetails(@RequestHeader HttpHeaders headers, @PathVariable String clubId);


    @GetMapping("api/wrc2023clubs/11/leaderboard/4YBzUBaw6PADpgTKf?MaxResultCount=10&FocusOnMe=false&Platform=0")
    CompletableFuture<Void> get();
}
