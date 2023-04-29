package io.busata.fourleft.endpoints.aggregators;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.application.aggregators.ClubMembersService;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Club;
import io.busata.fourleft.api.models.ClubMemberTo;
import io.busata.fourleft.domain.aggregators.ClubView;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import io.busata.fourleft.application.dirtrally2.racenet.ClubMemberFactory;
import io.busata.fourleft.application.dirtrally2.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ClubMembersEndpoint {
    private final ClubMembersService clubMembersService;
    private final ClubMemberFactory clubMemberToFactory;

    @GetMapping(Routes.CLUB_MEMBERS_BY_VIEW_ID)
    public List<ClubMemberTo> getClubMembers(@PathVariable UUID viewId) {
        return clubMembersService.getClubMembers(viewId).stream().map(clubMemberToFactory::create).collect(Collectors.toList());
    }
}
