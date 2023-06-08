package io.busata.fourleft.application.dirtrally2;

import io.busata.fourleft.application.dirtrally2.importer.clubs.RacenetSyncFacade;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final RacenetSyncFacade racenetSyncService;

    public Club getOrCreate(long clubId) {
        return clubRepository.findByReferenceId(clubId)
                .orElseGet(
                        () -> {
                            log.info("Unknown club {}, fetching from API", clubId);
                            racenetSyncService.importClub(clubId);
                            racenetSyncService.fullRefreshClub(clubId);
                            return clubRepository.getByReferenceId(clubId);
                        }
                );
    }

}
