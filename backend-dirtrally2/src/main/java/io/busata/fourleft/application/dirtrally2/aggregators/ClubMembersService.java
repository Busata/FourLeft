package io.busata.fourleft.application.dirtrally2.aggregators;

import io.busata.fourleft.application.dirtrally2.ClubService;
import io.busata.fourleft.domain.aggregators.ClubView;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubMembersService {
    private final ClubService clubService;
    private final ClubViewRepository clubViewRepository;

    public List<ClubMember> getClubMembers(UUID viewId) {

        ClubView clubView = clubViewRepository.findById(viewId).orElseThrow();

        Long clubId = clubView.getResultsView().getAssociatedClubs().stream().findFirst().orElseThrow();
        Club club = clubService.getOrCreate(clubId);


        return club.getClubMembers();

    }
}
