package io.busata.fourleft.application.dirtrally2.importer;

import io.busata.fourleft.api.events.ClubEventEnded;
import io.busata.fourleft.api.events.ClubEventStarted;
import io.busata.fourleft.api.events.ClubInactive;
import io.busata.fourleft.api.events.LeaderboardUpdated;
import io.busata.fourleft.application.dirtrally2.importer.clubs.RacenetSyncFacade;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubRepository;
import io.busata.fourleft.infrastructure.common.Usecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Usecase
@RequiredArgsConstructor
@Slf4j
public class ClubSyncUsecase {
    private final EventCleanService eventCleanService;
    private final ClubRepository clubRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RacenetSyncFacade racenetSyncFacade;

    public void updateLeaderboards() {
        cleanArchived();
        findClubs().stream().filter(club -> club.getErrorCount() < 5).forEach(club -> {
            try {
                updateClubDetails(club);
                updateClubLeaderboards(club);
            } catch (Exception ex) {
                log.warn("Error updating club {} - {}", club.getName(), club.getReferenceId(), ex);
                club.increaseErrorCount();
                clubRepository.save(club);
            }
        });
    }
    private void cleanArchived() {
        eventCleanService.cleanArchived();
    }
    private List<Club> findClubs() {
        return clubRepository.findAll();
    }
    private void updateClubDetails(Club club) {
         club.getCurrentEvent().ifPresentOrElse(event -> {
            if (event.hasEnded()) {
                log.info("-- Club {} has active event that ended, updating.", club.getName());
                racenetSyncFacade.fullRefreshClub(club.getReferenceId());

                applicationEventPublisher.publishEvent(new ClubEventEnded(club.getReferenceId()));

                log.info("-- Club {} had active event that ended, checking if new one started", club.getName());
                club.getCurrentEvent().ifPresent(newEvent -> {
                    applicationEventPublisher.publishEvent(new ClubEventStarted(club.getReferenceId()));
                });
            }

        }, () -> {
            if(club.requiresRefresh()) {
                log.info("-- Club {} has no active event,   reached refresh threshold, updating.", club.getName());
                racenetSyncFacade.fullRefreshClub(club.getReferenceId());
            }

            club.getCurrentEvent().ifPresentOrElse(newEvent -> {
                applicationEventPublisher.publishEvent(new ClubEventStarted(club.getReferenceId()));
            }, () -> {
                applicationEventPublisher.publishEvent(new ClubInactive(club.getReferenceId()));
            });
        });
    }

    private void updateClubLeaderboards(Club club) {
        club.getCurrentEvent().ifPresent(event -> {
            if(shouldUpdateLeaderboards(club, event)) {
                log.info("-- Updating leaderboards for {}", club.getName());
                racenetSyncFacade.refreshLeaderboards(club.getReferenceId());

                log.info("-- Update done.");

                applicationEventPublisher.publishEvent(new LeaderboardUpdated(club.getReferenceId()));
            }
        });
    }

    private boolean shouldUpdateLeaderboards(Club club, Event event) {
        if (club.getMembers() > 1500) {
            log.warn("-- Club {} has too many members, checking if hourly update required.", club.getName());
            return event.getLastResultCheckedTime() == null ||
                    Duration.between(event.getLastResultCheckedTime(), LocalDateTime.now()).toMinutes() >= 30;
        }

        return event.getLastResultCheckedTime() == null ||
                Duration.between(event.getLastResultCheckedTime(), LocalDateTime.now()).toMinutes() >= 10;
    }


}
