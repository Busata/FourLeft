package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchorEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
import io.busata.fourleft.common.ScoringStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the POINT_ANCHOR engine directly (no Spring context). Uses club 146's migrated 11-entry
 * definition and asserts it reproduces the old lookup table where it matters: positions 1..203 exactly,
 * the pinned midpoint/endpoint anchors, the floored deep tail, and the fractional-decrease zigzag.
 */
class ScoringServicePointAnchorTest {

    private final ScoringService scoringService = new ScoringService();

    private static ScoringAnchorEntry anchor(int position, int points) {
        return new ScoringAnchorEntry(position, points, null);
    }

    private static ScoringAnchorEntry decrease(int position, String amount) {
        return new ScoringAnchorEntry(position, null, new BigDecimal(amount));
    }

    /** Club 146's migrated definition — mirrors V028__point_anchor_scoring.sql. */
    private static DiscordClubConfiguration club146() {
        ScoringAnchors anchors = new ScoringAnchors(1, List.of(
                anchor(1, 2500),
                anchor(2, 2200),
                decrease(3, "100"),
                anchor(4, 2025),
                decrease(5, "25"),
                decrease(7, "15"),
                decrease(10, "5"),
                decrease(26, "2"),
                decrease(204, "1.83"),
                anchor(788, 424),
                decrease(789, "1.99")
        ));
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 1L, "146", true);
        config.setScoringStrategy(ScoringStrategy.POINT_ANCHOR);
        config.setScoringAnchors(anchors);
        return config;
    }

    @Test
    void reproducesTheDesignedHeadExactly() {
        DiscordClubConfiguration config = club146();
        // A sample of the old LOOKUP_TABLE values, which positions 1..203 must match byte-for-byte.
        int[][] expected = {
                {1, 2500}, {2, 2200}, {3, 2100}, {4, 2025}, {5, 2000}, {6, 1975},
                {7, 1960}, {8, 1945}, {9, 1930}, {10, 1925}, {11, 1920}, {25, 1850},
                {26, 1848}, {50, 1800}, {100, 1700}, {200, 1500}, {203, 1494}
        };
        for (int[] pair : expected) {
            assertThat(scoringService.getPoints(config, pair[0]))
                    .as("position %d", pair[0])
                    .isEqualTo(pair[1]);
        }
    }

    @Test
    void pinsMidpointAndEndpointAnchors() {
        DiscordClubConfiguration config = club146();
        assertThat(scoringService.getPoints(config, 788)).isEqualTo(424);
        assertThat(scoringService.getPoints(config, 1000)).isEqualTo(2);
    }

    @Test
    void floorsDeepRanksToOne() {
        DiscordClubConfiguration config = club146();
        assertThat(scoringService.getPoints(config, 1001)).isEqualTo(1);
        assertThat(scoringService.getPoints(config, 5000)).isEqualTo(1);
    }

    @Test
    void fractionalDecreaseTracksTheTailWithinTolerance() {
        DiscordClubConfiguration config = club146();
        // Old table: 300 -> 1325, 500 -> 950. The 1.83 segment approximates within a handful of points.
        assertThat(scoringService.getPoints(config, 300)).isBetween(1305, 1325);
        assertThat(scoringService.getPoints(config, 500)).isBetween(940, 960);
    }

    @Test
    void unconfiguredAnchorsFallBackToDefault() {
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 1L, "146", true);
        config.setScoringStrategy(ScoringStrategy.POINT_ANCHOR);
        // scoringAnchors left null
        assertThat(scoringService.getPoints(config, 1)).isEqualTo(1);
    }

    @Test
    void bareAnchorDoesNotPropagateForward() {
        // anchor 1=2500, anchor 5=2000, decrease @8 = -10: positions 2-4 and 6-7 fall to the floor.
        ScoringAnchors anchors = new ScoringAnchors(1, List.of(
                anchor(1, 2500), anchor(5, 2000), decrease(8, "10")));
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 1L, "x", true);
        config.setScoringStrategy(ScoringStrategy.POINT_ANCHOR);
        config.setScoringAnchors(anchors);

        assertThat(scoringService.getPoints(config, 1)).isEqualTo(2500);
        assertThat(scoringService.getPoints(config, 3)).isEqualTo(1);   // default gap
        assertThat(scoringService.getPoints(config, 5)).isEqualTo(2000);
        assertThat(scoringService.getPoints(config, 7)).isEqualTo(1);   // default gap
        assertThat(scoringService.getPoints(config, 8)).isEqualTo(1990);
        assertThat(scoringService.getPoints(config, 9)).isEqualTo(1980);
    }
}
