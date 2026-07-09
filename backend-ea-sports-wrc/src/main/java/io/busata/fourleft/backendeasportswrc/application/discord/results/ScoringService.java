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

    /** For strategies that don't depend on the field size. RACENET_DEFAULT scores 0 through here. */
    public int getPoints(DiscordClubConfiguration configuration, int position) {
        return getPoints(configuration, position, 0);
    }

    public int getPoints(DiscordClubConfiguration configuration, int position, int fieldSize) {
        ScoringStrategy strategy = configuration.getScoringStrategy() != null
                ? configuration.getScoringStrategy()
                : ScoringStrategy.LOOKUP_TABLE;

        return switch (strategy) {
            case LOOKUP_TABLE -> lookup(configuration.getScoringTable(), position);
            case POINT_ANCHOR -> anchorPoints(configuration.getScoringAnchors(), position);
            case RACENET_DEFAULT -> racenetDefaultPoints(position, fieldSize);
        };
    }

    /**
     * Racenet's default participation-scaled system: with a field of {@code fieldSize} entrants,
     * position {@code rank} scores {@code max(0, floor(P * (3r + 1) / (4r)) - (r - 1))}. The winner
     * gets exactly P, points fall to a linear ...3, 2, 1 tail and roughly the bottom quarter of the
     * field scores 0. Reverse-engineered from racenet standings; exact on every observed field size
     * (P = 4 to 1297). Racenet counts everyone who started the event in P, DNFs included.
     */
    public static int racenetDefaultPoints(int rank, int fieldSize) {
        if (rank < 1 || fieldSize < 1) {
            return 0;
        }
        long points = (long) fieldSize * (3L * rank + 1) / (4L * rank) - (rank - 1);
        return (int) Math.max(0, points);
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
