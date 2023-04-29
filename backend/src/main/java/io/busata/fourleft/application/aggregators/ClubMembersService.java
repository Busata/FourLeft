package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.application.dirtrally2.importer.ClubSyncService;
import io.busata.fourleft.domain.aggregators.ClubView;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.models.ClubMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubMembersService {
    private final ClubSyncService clubSyncService;
    private final ClubViewRepository clubViewRepository;

    public List<ClubMember> getClubMembers(UUID viewId) {

        ClubView clubView = clubViewRepository.findById(viewId).orElseThrow();

        Long clubId = clubView.getResultsView().getAssociatedClubs().stream().findFirst().orElseThrow();
        Club club = clubSyncService.getOrCreate(clubId);

        return club.getClubMembers();

    }
}
