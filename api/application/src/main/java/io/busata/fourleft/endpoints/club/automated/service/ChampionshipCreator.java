package io.busata.fourleft.endpoints.club.automated.service;

import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.importer.ClubSyncService;
import io.busata.fourleft.common.TransactionHandler;
import io.busata.fourleft.gateway.racenet.RacenetGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChampionshipCreator {

    private final RacenetGateway client;
    private final DailyChampionshipCreator dailyChampionshipCreator;
    private final WeeklyChampionshipCreator weeklyChampionshipCreator;
    private final TransactionHandler transactionHandler;

    private final ClubRepository clubRepository;

    private final ClubSyncService clubSyncService;

    public void createDailyChampionship(long clubId) {
        log.info("Creating daily championship");

        client.createChampionship(clubId, dailyChampionshipCreator.createEvent(clubId));

        log.info("Championship created!");

        transactionHandler.runInTransaction(() -> {
            clubRepository.findByReferenceId(clubId).ifPresent(clubSyncService::refreshClubDetails);
        });
    }

    public void createWeeklyChampionship(long clubId) {
        log.info("Creating monthly championship");

        client.createChampionship(clubId, weeklyChampionshipCreator.createEvent(clubId));

        log.info("Championship created");

        transactionHandler.runInTransaction(() -> {
            clubRepository.findByReferenceId(clubId).ifPresent(clubSyncService::refreshClubDetails);
        });
    }


    public void refreshDaily(long clubId) {
        log.info("Refreshing daily championship");

        transactionHandler.runInTransaction(() -> {
            clubRepository
                    .findByReferenceId(clubId)
                    .ifPresent(clubSyncService::fullRefreshClub);
        });
    }
    public void refreshWeekly(long clubId) {
        log.info("Refreshing weekly championship");

        transactionHandler.runInTransaction(() -> {
            clubRepository
                    .findByReferenceId(clubId)
                    .ifPresent(clubSyncService::fullRefreshClub);
        });
    }
}
