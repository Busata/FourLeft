package io.busata.fourleft.backendeasportswrc.application.importer.process;


import io.busata.fourleft.backendeasportswrc.application.importer.results.ClubImportResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardImportResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.StandingsImportResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Getter
public class ClubImportProcess {

    private ProcessState state;

    public void setState(ProcessState state) {
        if(!(this.state == ProcessState.START && state == ProcessState.DONE)) {
            log.info("Club {} • {} -> {}", this.clubId, this.state, state);
        }
        this.state = state;
    }

    private final String clubId;

    @Setter
    private CompletableFuture<ClubImportResult> details;

    @Setter
    private List<CompletableFuture<StandingsImportResult>> standings;

    @Setter
    private List<CompletableFuture<LeaderboardImportResult>> leaderboards;


    public ClubImportProcess(String clubId) {
        this.state = ProcessState.START;
        this.clubId = clubId;
    }

    public boolean isDetailsFetchingDone() {
        return this.details.isDone();
    }

    public boolean isLeaderboardFetchingDone() {
        return leaderboards.stream().allMatch(CompletableFuture::isDone);
    }

    public boolean isStandingsFetchingDone() {
        return standings.stream().allMatch(CompletableFuture::isDone);
    }

    public boolean isDone() {
        return this.state == ProcessState.DONE || this.state == ProcessState.FAILED;
    }

    public boolean canContinue() {
        return state.isKeepProcessing() && !isDone();
    }

    public void markDone() {
        setState(ProcessState.DONE);
    }

    public void markFailed() {
        setState(ProcessState.FAILED);
    }
}


