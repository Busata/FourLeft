package io.busata.fourleft.backendeasportswrc.application.importer;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper.parseDuration;

@Service
public class LeaderboardFactory {

    public ClubLeaderboardEntry createEntry(ClubLeaderboardEntryTo entryTo, Long rankAccumulative, Duration differenceAccumulated) {
        return ClubLeaderboardEntry.builder()
                .displayName(entryTo.displayName())
                .nationalityID(entryTo.nationalityID())
                .platform(entryTo.platform())
                .rank(entryTo.rank())
                .rankAccumulated(rankAccumulative)
                .vehicle(entryTo.vehicle())
                .wrcPlayerId(entryTo.wrcPlayerId())
                .ssid(entryTo.ssid())
                .time(parseDuration(entryTo.time()))
                .timePenalty(parseDuration(entryTo.timePenalty()))
                .timeAccumulated(parseDuration(entryTo.timeAccumulated()))
                .differenceAccumulated(differenceAccumulated)
                .differenceToFirst(parseDuration(entryTo.differenceToFirst()))
                .build();
    }

}
