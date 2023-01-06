package io.busata.fourleft.importer;

import io.busata.fourleft.api.messages.ClubEventEnded;
import io.busata.fourleft.api.messages.ClubEventStarted;
import io.busata.fourleft.api.messages.LeaderboardUpdated;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.importer.updaters.EventCleanService;
import io.busata.fourleft.importer.updaters.RacenetClubMemberSyncService;
import io.busata.fourleft.importer.updaters.RacenetClubSyncService;
import io.busata.fourleft.importer.updaters.RacenetLeaderboardSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    private final EntityManager entityManager;

    @Transactional
    public void updateClubs() {
        log.info("Start updating clubs.");
        eventCleanService.cleanArchived();

        clubRepository.findAll()
                .forEach(this::doUpdate);
        log.info("End updating clubs.");
    }

    protected void doUpdate(Club club) {
        club.getCurrentEvent().ifPresentOrElse(event -> {
            if (event.hasEnded()) {
                log.info("-- Club {} has active event that ended, updating.", club.getName());
                fullRefreshClub(club);

                applicationEventPublisher.publishEvent(new ClubEventEnded(club.getReferenceId()));

                log.info("-- Club {} had active event that ended, checking if new one started", club.getName());
                club.getCurrentEvent().ifPresent(newEvent -> {
                    applicationEventPublisher.publishEvent(new ClubEventStarted(club.getReferenceId()));
                });
            }

            if(shouldUpdateLeaderboards(event)) {
                log.info("-- Updating leaderboards for {}", club.getName());
                refreshLeaderboards(club);
                log.info("-- Update done.");

                applicationEventPublisher.publishEvent(new LeaderboardUpdated(club.getReferenceId()));
            }

        }, () -> {
            if(club.requiresRefresh()) {
                log.info("-- Club {} has no active event, reached refresh threshold, updating.", club.getName());
                fullRefreshClub(club);
            }

            club.getCurrentEvent().ifPresent(newEvent -> {
                applicationEventPublisher.publishEvent(new ClubEventStarted(club.getReferenceId()));
            });
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

        clubRepository.saveAndFlush(club);
        entityManager.refresh(club);
    }

    public void refreshClubDetails(Club club) {
        racenetClubSyncService.syncWithRacenet(club);

        clubRepository.saveAndFlush(club);
        entityManager.refresh(club);

    }

    public void refreshLeaderboards(Club club) {
        racenetLeaderboardSyncService.syncWithRacenet(club);

        clubRepository.saveAndFlush(club);
        entityManager.refresh(club);
    }
    public void refreshMembers(Club club) {
        racenetClubMemberSyncService.syncWithRacenet(club);

        clubRepository.saveAndFlush(club);
        entityManager.refresh(club);

    }


}
