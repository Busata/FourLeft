package io.busata.fourleft.application.dirtrally2.importer.clubs;

import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class RacenetSyncFacade {

    private final ClubRepository clubRepository;
    private final RacenetClubSyncService racenetClubSyncService;

    private final RacenetLeaderboardSyncService racenetLeaderboardSyncService;
    private final RacenetClubMemberSyncService racenetClubMemberSyncService;


    @Transactional
    public void importClub(long clubId) {
        Club club = new Club();
        club.setReferenceId(clubId);

        clubRepository.save(club);
    }

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
