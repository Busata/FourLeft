package io.busata.fourleft.importer;

import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.importer.updaters.EventCleanService;
import io.busata.fourleft.importer.updaters.RacenetClubMemberSyncService;
import io.busata.fourleft.importer.updaters.RacenetClubSyncService;
import io.busata.fourleft.importer.updaters.RacenetLeaderboardSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClubSyncService {
    private final EventCleanService eventCleanService;
    private final ClubRepository clubRepository;
    private final RacenetClubSyncService racenetClubSyncService;
    private final RacenetLeaderboardSyncService racenetLeaderboardSyncService;
    private final RacenetClubMemberSyncService racenetClubMemberSyncService;

    @Transactional
    public void updateClubs() {
        eventCleanService.cleanArchived();

        clubRepository.findAll()
                .forEach(this::doUpdate);
    }

    protected void doUpdate(Club club) {
        club.getCurrentEvent().ifPresentOrElse(event -> {
            if (event.hasEnded()) {
                log.info("Club {} has active event that ended, updating.", club.getName());
                fullRefreshClub(club);
            }

            if(shouldUpdateLeaderboards(event)) {
                log.info("Updating leaderboards for {}", club.getName());
                refreshLeaderboards(club);
                log.info("Update done.");
            }

        }, () -> {
            if(club.requiresRefresh()) {
                log.info("Club {} has no active event, reached refresh threshold, updating.", club.getName());
                fullRefreshClub(club);
            }
        });
    }

    private boolean shouldUpdateLeaderboards(Event event) {
        return event.getLastResultCheckedTime() == null ||
                Duration.between(event.getLastResultCheckedTime(), LocalDateTime.now()).toMinutes() >= 10;
    }

    @Transactional
    public Club getOrCreate(long clubId) {
        return clubRepository.findByReferenceId(clubId)
                .orElseGet(
                        () -> {
                            log.info("Unknown club {}, fetching from API", clubId);
                            Club club = createClub(clubId);
                            return clubRepository.save(club);
                        }
                );
    }

    private Club createClub(long clubId) {
        Club club = new Club();
        club.setReferenceId(clubId);

        fullRefreshClub(club);

        return club;
    }

    public void fullRefreshClub(Club club) {
        refreshClubDetails(club);
        refreshLeaderboards(club);
        refreshMembers(club);
    }

    public void refreshClubDetails(Club club) {
        racenetClubSyncService.syncWithRacenet(club);
        clubRepository.save(club);
    }

    public void refreshLeaderboards(Club club) {
        racenetLeaderboardSyncService.syncWithRacenet(club);
    }
    public void refreshMembers(Club club) {
        racenetClubMemberSyncService.syncWithRacenet(club);
    }


}
