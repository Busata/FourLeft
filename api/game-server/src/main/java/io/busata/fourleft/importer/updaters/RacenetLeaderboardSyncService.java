package io.busata.fourleft.importer.updaters;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import feign.RetryableException;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetLeaderboardSyncService {

    private final EventRepository eventRepository;
    private final LeaderboardFetcher leaderboardFetcher;

    public void syncWithRacenet(Club club) {
        try {
            club.getCurrentEvent().ifPresent(this::updateEventLeaderboards);
        } catch (Exception ex) {
            log.error("-- -- Error while updating current leaderboards", ex);
        }

        try {
            club.getPreviousEvent()
                    .filter(event -> event.getLastResultCheckedTime() == null)
                    .ifPresent(this::updateEventLeaderboards);
        } catch (Exception ex) {
            log.error("-- -- Error while updating previous leaderboard", ex);
        }
    }

    private void updateEventLeaderboards(Event event) {
        ImmutableList.copyOf(event.getStages()).forEach(stage -> {
            try {
                log.info("-- -- Updating for event {}, stage {}", event.getName(), stage.getName());
                leaderboardFetcher.upsertBoard(event.getChallengeId(), event.getReferenceId(), String.valueOf(stage.getReferenceId()), true);
                event.setLastResultCheckedTime(LocalDateTime.now());
                eventRepository.save(event);
            } catch (FeignException.InternalServerError | RetryableException ex) {
                log.error("Internal server error {}", ex.getMessage());
                if(ex.status() == 500 && ex.getMessage().contains("was not found")) {
                    event.markAsRemoved();
                    eventRepository.save(event);
                } else {
                    throw ex;
                }
            } catch (Exception ex) {
                log.error("-- -- -- Couldn't update event {} {} {}", event.getName(), event.getCountry(), ex);
                event.setLastResultCheckedTime(LocalDateTime.now());
                eventRepository.save(event);
            }
        });
    }
}
