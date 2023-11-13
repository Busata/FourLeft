package io.busata.fourleft.backendeasportswrc.application.importer.results;

import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardImportResult;
import lombok.Getter;

@Getter
public class LeaderboardImportResultFailed extends LeaderboardImportResult {

    private final String clubId;
    private final String leaderboardId;
    private final String message;

    public LeaderboardImportResultFailed(String clubId, String leaderboardId, String message) {
        this.clubId = clubId;
        this.leaderboardId = leaderboardId;
        this.message = message;
    }
}
