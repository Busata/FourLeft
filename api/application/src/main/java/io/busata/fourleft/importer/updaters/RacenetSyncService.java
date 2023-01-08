package io.busata.fourleft.importer.updaters;

import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@RequiredArgsConstructor
public class RacenetSyncService {

    private final ClubRepository clubRepository;
    private final RacenetClubSyncService racenetClubSyncService;

    private final RacenetLeaderboardSyncService racenetLeaderboardSyncService;
    private final RacenetClubMemberSyncService racenetClubMemberSyncService;

    @Transactional
    public void fullRefreshClub(long clubId) {
        final var club = clubRepository.findByReferenceId(clubId).orElseThrow();
        racenetClubSyncService.syncWithRacenet(club);
        racenetClubMemberSyncService.syncWithRacenet(club);
        racenetLeaderboardSyncService.syncWithRacenet(club);
    }


    @Transactional
    public void refreshLeaderboards(long clubId) {
        final var club = clubRepository.findByReferenceId(clubId).orElseThrow();
        racenetLeaderboardSyncService.syncWithRacenet(club);
    }
}
