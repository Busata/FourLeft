package io.busata.fourleft.importer.updaters;

import io.busata.fourleft.domain.clubs.models.Club;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@RequiredArgsConstructor
public class RacenetSyncService {
    private final RacenetClubSyncService racenetClubSyncService;

    private final RacenetLeaderboardSyncService racenetLeaderboardSyncService;
    private final RacenetClubMemberSyncService racenetClubMemberSyncService;

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void fullRefreshClub(Club club) {
        racenetClubSyncService.syncWithRacenet(club);
        racenetClubMemberSyncService.syncWithRacenet(club);
        racenetLeaderboardSyncService.syncWithRacenet(club);
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void refreshLeaderboards(Club club) {
        racenetLeaderboardSyncService.syncWithRacenet(club);
    }
}
