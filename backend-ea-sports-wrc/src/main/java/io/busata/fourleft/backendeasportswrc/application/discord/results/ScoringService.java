package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchorEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
import io.busata.fourleft.common.ScoringStrategy;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
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
            case POINT_ANCHOR -> anchorPoints(configuration.getScoringAnchors(), position);
        };
    }

    private int lookup(Map<String, Integer> table, int position) {
        if (table == null) {
            return DEFAULT_POINTS;
        }
        return table.getOrDefault(Integer.toString(position), DEFAULT_POINTS);
    }

    /**
     * Expands a {@link ScoringAnchors} definition to the points for a single position. Walks positions
     * {@code 1..position} in hundredths of a point (integer math, no float drift): an anchor sets the
     * running value and stops any active decrease; a decrease subtracts its per-position amount and keeps
     * doing so until a later entry overrides it; a position covered by neither scores the floor.
     */
    private int anchorPoints(ScoringAnchors anchors, int position) {
        if (anchors == null || anchors.entries() == null || anchors.entries().isEmpty()) {
            return DEFAULT_POINTS;
        }

        final int floor = anchors.floor();
        List<ScoringAnchorEntry> entries = anchors.entries().stream()
                .sorted(Comparator.comparingInt(ScoringAnchorEntry::position))
                .toList();

        Long runningHundredths = null;   // current true value * 100; null while no value/decrease is in effect
        long stepHundredths = 0;         // active decrease per position * 100
        boolean decreaseActive = false;
        int entryIndex = 0;
        int result = floor;

        for (int p = 1; p <= position; p++) {
            boolean handled = false;

            while (entryIndex < entries.size() && entries.get(entryIndex).position() == p) {
                ScoringAnchorEntry entry = entries.get(entryIndex++);
                if (entry.isAnchor()) {
                    runningHundredths = entry.points() * 100L;
                    decreaseActive = false;
                } else {
                    stepHundredths = entry.decrease()
                            .movePointRight(2)
                            .setScale(0, RoundingMode.HALF_UP)
                            .longValueExact();
                    decreaseActive = true;
                    if (runningHundredths != null) {
                        runningHundredths -= stepHundredths;
                    }
                }
                handled = true;
            }

            if (!handled && decreaseActive && runningHundredths != null) {
                runningHundredths -= stepHundredths;
                handled = true;
            }

            result = (handled && runningHundredths != null)
                    ? Math.max(roundHundredths(runningHundredths), floor)
                    : floor;
        }

        return result;
    }

    /** Rounds a hundredths-of-a-point value to whole points, half up (matches Java {@code Math.round}). */
    private static int roundHundredths(long hundredths) {
        return hundredths >= 0
                ? (int) ((hundredths + 50) / 100)
                : (int) -((-hundredths + 50) / 100);
    }
}
