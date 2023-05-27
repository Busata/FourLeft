package io.busata.fourleft.application.dirtrally2.importer.updaters;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import feign.RetryableException;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.dirtrally2.clubs.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
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
            club.findActiveChampionship().stream().flatMap(championship -> championship.getEvents().stream())
                    .filter(event -> event.getLastResultCheckedTime() == null)
                    .forEach(this::updateEventLeaderboards);
        } catch (Exception ex) {
            log.error("-- -- Error while updating unsynced events of championship", ex);
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
