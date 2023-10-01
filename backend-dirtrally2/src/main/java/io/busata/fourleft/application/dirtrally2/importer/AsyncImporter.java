package io.busata.fourleft.application.dirtrally2.importer;

import io.busata.fourleft.api.events.ClubEventStarted;
import io.busata.fourleft.api.events.ClubInactive;
import io.busata.fourleft.api.events.LeaderboardUpdated;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncImporter {

    private final ClubRepository clubRepository;
    private final ApplicationEventPublisher applicationEventPublisher;




    public void update() {

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        clubRepository.findAll().forEach(club -> {
            club.getCurrentEvent().ifPresentOrElse(event -> {
                if (event.hasEnded()) {
                    futures.add(fullUpdate(club.getReferenceId(), (result) -> {
                        applicationEventPublisher.publishEvent(new ClubEventStarted(club.getReferenceId()));

                        club.getCurrentEvent().ifPresent(newEvent -> {
                            applicationEventPublisher.publishEvent(new ClubEventStarted(club.getReferenceId()));
                        });
                    }));

                } else if (shouldUpdateLeaderboards(club, event)) {
                    futures.add(updateBoards(club.getReferenceId(), (result) -> {
                        applicationEventPublisher.publishEvent(new LeaderboardUpdated(club.getReferenceId()));
                    }));
                }
            }, () -> {
                if (club.requiresRefresh()) {
                    futures.add(fullUpdate(club.getReferenceId(), (result) -> {
                        club.getCurrentEvent().ifPresentOrElse(newEvent -> {
                            applicationEventPublisher.publishEvent(new ClubEventStarted(club.getReferenceId()));
                        }, () -> {
                            applicationEventPublisher.publishEvent(new ClubInactive(club.getReferenceId()));
                        });
                    }));
                }
            });
        });

        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        try {
            voidCompletableFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }



    private CompletableFuture<Void> fullUpdate(long clubId, Consumer<String> completed) {
        return CompletableFuture.supplyAsync(() -> {
            return "";
        }).thenAccept(completed);

    }

    private CompletableFuture<Void> updateBoards(long clubId, Consumer<Boolean> completed) {
        return CompletableFuture.supplyAsync(() -> {
            return false;
        }).thenAccept(completed);
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
