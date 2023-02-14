package io.busata.fourleft.endpoints.club.members;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.api.models.ClubMemberTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
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
    private final ClubViewRepository clubViewRepository;


    @GetMapping(Routes.CLUB_MEMBERS_BY_VIEW_ID)
    public List<ClubMemberTo> getClubMembers(@PathVariable UUID viewId) {

        ClubView clubView = clubViewRepository.findById(viewId).orElseThrow();

        Long clubId = clubView.getResultsView().getAssociatedClubs().stream().findFirst().orElseThrow();
        Club club = clubSyncService.getOrCreate(clubId);

        return club.getClubMembers().stream().map(clubMemberToFactory::create).collect(Collectors.toList());

    }
}
