package io.busata.fourleft.backendeasportswrc.application.importer.results;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardResultTo;
import lombok.Getter;

import java.util.List;

public class LeaderboardUpdatedResult extends LeaderboardImportResult {

    @Getter
    private final String clubId;

    @Getter
    private final String leaderboardId;

    @Getter
    private final List<ClubLeaderboardEntryTo> entries;

    public LeaderboardUpdatedResult(String clubId, String leaderboardId, List<ClubLeaderboardEntryTo> entries) {
        this.clubId = clubId;
        this.leaderboardId = leaderboardId;
        this.entries = entries;
    }
}
