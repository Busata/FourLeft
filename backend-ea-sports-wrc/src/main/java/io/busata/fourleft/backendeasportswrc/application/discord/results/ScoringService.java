package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.common.ScoringStrategy;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Resolves the points awarded for a finishing position under a channel's custom scoring config.
 * Replaces the old hardcoded {@code CustomPoints} table that only applied to club 146.
 */
@Service
public class ScoringService {

    /** Points for a position not covered by the strategy (matches the old table's getOrDefault). */
    private static final int DEFAULT_POINTS = 1;

    public int getPoints(DiscordClubConfiguration configuration, int position) {
        ScoringStrategy strategy = configuration.getScoringStrategy() != null
                ? configuration.getScoringStrategy()
                : ScoringStrategy.LOOKUP_TABLE;

        return switch (strategy) {
            case LOOKUP_TABLE -> lookup(configuration.getScoringTable(), position);
        };
    }

    private int lookup(Map<String, Integer> table, int position) {
        if (table == null) {
            return DEFAULT_POINTS;
        }
        return table.getOrDefault(Integer.toString(position), DEFAULT_POINTS);
    }
}
