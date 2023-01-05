package io.busata.fourleft.importer;

import io.busata.fourleft.api.messages.ClubOperation;
import io.busata.fourleft.api.messages.ClubUpdated;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.importer.updaters.EventCleanService;
import io.busata.fourleft.importer.updaters.RacenetClubMemberSyncService;
import io.busata.fourleft.importer.updaters.RacenetClubSyncService;
import io.busata.fourleft.importer.updaters.RacenetLeaderboardSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;

import static io.busata.fourleft.api.messages.QueueNames.CLUB_EVENT_QUEUE;

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

                applicationEventPublisher.publishEvent(new ClubUpdated(ClubOperation.EVENT_ENDED, club.getReferenceId()));


                club.getCurrentEvent().filter(Event::isCurrent).ifPresent(newEvent -> {
                    applicationEventPublisher.publishEvent(new ClubUpdated(ClubOperation.EVENT_STARTED, club.getReferenceId()));
                });
            }

            if(shouldUpdateLeaderboards(event)) {
                log.info("Updating leaderboards for {}", club.getName());
                refreshLeaderboards(club);
                log.info("Update done.");

                applicationEventPublisher.publishEvent(new ClubUpdated(ClubOperation.LEADERBOARDS_UPDATED, club.getReferenceId()));
            }

        }, () -> {
            if(club.requiresRefresh()) {
                log.info("Club {} has no active event, reached refresh threshold, updating.", club.getName());
                fullRefreshClub(club);
            }

            club.getCurrentEvent().filter(Event::isCurrent).ifPresent(newEvent -> {
                applicationEventPublisher.publishEvent(new ClubUpdated(ClubOperation.EVENT_STARTED, club.getReferenceId()));
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
