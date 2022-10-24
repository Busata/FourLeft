package io.busata.fourleft.endpoints.club.members;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.api.models.ClubMemberTo;
import io.busata.fourleft.gateway.racenet.factory.ClubMemberFactory;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClubMembersEndpoint {
    private final ClubSyncService clubSyncService;
    private final ClubMemberFactory clubMemberToFactory;


    @GetMapping(Routes.CLUB_MEMBERS_BY_CLUB_ID)
    public List<ClubMemberTo> getClubMembers(@PathVariable UUID viewId) {
        //Club club = clubSyncService.getOrCreate(clubId);

        //return club.getClubMembers().stream().map(clubMemberToFactory::create).collect(Collectors.toList());

        return List.of();
    }
}
