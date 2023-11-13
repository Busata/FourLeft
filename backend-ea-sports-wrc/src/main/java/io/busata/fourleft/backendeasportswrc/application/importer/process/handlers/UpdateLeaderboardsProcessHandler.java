package io.busata.fourleft.backendeasportswrc.application.importer.process.handlers;

import io.busata.fourleft.api.easportswrc.events.LeaderboardUpdatedEvent;
import io.busata.fourleft.backendeasportswrc.application.importer.importers.ClubLeaderboardsImporter;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardImportResultFailed;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ClubImportProcess;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ClubImportProcessHandler;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ProcessState;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardUpdatedResult;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateLeaderboardsProcessHandler implements ClubImportProcessHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;
    private final ClubLeaderboardsImporter leaderboardsImporter;

    @Getter
    private final Map<ProcessState, Consumer<ClubImportProcess>> strategies = Map.of(
            ProcessState.CHECK_LEADERBOARDS, this::processCheckLeaderboards,
            ProcessState.FETCHING_LEADERBOARDS, this::processFetchingLeaderboards,
            ProcessState.FETCHING_LEADERBOARDS_FINISHED, this::processFetchingLeaderboardsFinished
    );

    private void processCheckLeaderboards(ClubImportProcess process) {
        List<String> leaderboardsRequiringUpdate = this.clubService.getLeaderboardsRequiringUpdate(process.getClubId());
        if (leaderboardsRequiringUpdate.isEmpty()) {
            process.markDone();
        } else {
            process.setLeaderboards(leaderboardsRequiringUpdate.stream().map(leaderboardId -> {
                return leaderboardsImporter.importLeaderboard(process.getClubId(), leaderboardId);
            }).toList());
            process.setState(ProcessState.FETCHING_LEADERBOARDS);
        }
    }

    private void processFetchingLeaderboards(ClubImportProcess process) {
        if (process.getLeaderboards().stream().allMatch(CompletableFuture::isDone)) {
            process.setState(ProcessState.FETCHING_LEADERBOARDS_FINISHED);
        }
    }


    private void processFetchingLeaderboardsFinished(ClubImportProcess process) {
        process.getLeaderboards().stream()
                .map(CompletableFuture::join)
                .filter(LeaderboardUpdatedResult.class::isInstance)
                .map(LeaderboardUpdatedResult.class::cast)
                .forEach(this.clubLeaderboardService::updateLeaderboards);

        process.getLeaderboards().stream()
                .map(CompletableFuture::join)
                .filter(LeaderboardImportResultFailed.class::isInstance)
                .map(LeaderboardImportResultFailed.class::cast)
                .forEach(board -> {
                    log.error("Club {} • Board {} • Failed to import: {}", board.getClubId(), board.getLeaderboardId(), board.getMessage());
                });

        this.eventPublisher.publishEvent(new LeaderboardUpdatedEvent(process.getClubId()));

        process.markDone();
    }
}
