package io.busata.fourleft.importer.updaters;

import io.busata.fourleft.domain.clubs.models.Club;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RacenetSyncService {
    private final RacenetClubSyncService racenetClubSyncService;

    private final RacenetLeaderboardSyncService racenetLeaderboardSyncService;
    private final RacenetClubMemberSyncService racenetClubMemberSyncService;

    public void fullRefreshClub(Club club) {
        racenetClubSyncService.syncWithRacenet(club);
        racenetClubMemberSyncService.syncWithRacenet(club);
        racenetLeaderboardSyncService.syncWithRacenet(club);
    }

    public void refreshLeaderboards(Club club) {
        racenetLeaderboardSyncService.syncWithRacenet(club);
    }
}
