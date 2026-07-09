package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.common.ScoringStrategy;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the RACENET_DEFAULT formula against points racenet itself awarded, taken from finished
 * single-event championships in the production data (standings points_accumulated by rank). The
 * formula — max(0, floor(P * (3r + 1) / (4r)) - (r - 1)) — reproduced every one of the 95 distinct
 * field sizes observed (P = 4 to 1297); the vectors below are a spread of those.
 */
class ScoringServiceRacenetDefaultTest {

    private final ScoringService scoringService = new ScoringService();

    private static DiscordClubConfiguration racenetDefault() {
        DiscordClubConfiguration configuration = new DiscordClubConfiguration();
        configuration.setScoringStrategy(ScoringStrategy.RACENET_DEFAULT);
        return configuration;
    }

    private static int[] expand(int fieldSize) {
        return IntStream.rangeClosed(1, fieldSize)
                .map(rank -> ScoringService.racenetDefaultPoints(rank, fieldSize))
                .toArray();
    }

    @Test
    void reproducesObservedRacenetVectors() {
        assertThat(expand(4)).containsExactly(4, 2, 1, 0);
        assertThat(expand(7)).containsExactly(7, 5, 3, 2, 1, 0, 0);
        assertThat(expand(10)).containsExactly(10, 7, 6, 5, 4, 2, 1, 0, 0, 0);
        assertThat(expand(17)).containsExactly(17, 13, 12, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0, 0, 0);
        assertThat(expand(25)).containsExactly(25, 20, 18, 17, 16, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5,
                4, 3, 2, 1, 0, 0, 0, 0, 0, 0);
        assertThat(expand(31)).containsExactly(31, 26, 23, 22, 20, 19, 18, 17, 16, 15, 13, 12, 11, 10,
                9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(expand(40)).containsExactly(40, 34, 31, 29, 28, 26, 25, 24, 23, 22, 20, 19, 18, 17,
                16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /** The P=1297 championship pins the coefficients: any drifted fraction breaks these head values. */
    @Test
    void reproducesTheGiantFieldHead() {
        assertThat(ScoringService.racenetDefaultPoints(1, 1297)).isEqualTo(1297);
        assertThat(ScoringService.racenetDefaultPoints(2, 1297)).isEqualTo(1133);
        assertThat(ScoringService.racenetDefaultPoints(3, 1297)).isEqualTo(1078);
        assertThat(ScoringService.racenetDefaultPoints(4, 1297)).isEqualTo(1050);
        assertThat(ScoringService.racenetDefaultPoints(5, 1297)).isEqualTo(1033);
        assertThat(ScoringService.racenetDefaultPoints(10, 1297)).isEqualTo(996);
    }

    @Test
    void winnerAlwaysScoresTheFieldSize() {
        for (int fieldSize = 1; fieldSize <= 200; fieldSize++) {
            assertThat(ScoringService.racenetDefaultPoints(1, fieldSize)).isEqualTo(fieldSize);
        }
    }

    @Test
    void resolvesThroughTheStrategySwitch() {
        assertThat(scoringService.getPoints(racenetDefault(), 1, 17)).isEqualTo(17);
        assertThat(scoringService.getPoints(racenetDefault(), 5, 17)).isEqualTo(9);
        assertThat(scoringService.getPoints(racenetDefault(), 14, 17)).isZero();
    }

    @Test
    void guardsDegenerateInputs() {
        assertThat(ScoringService.racenetDefaultPoints(0, 20)).isZero();
        assertThat(ScoringService.racenetDefaultPoints(5, 0)).isZero();
        assertThat(ScoringService.racenetDefaultPoints(-1, -1)).isZero();
        // Positions past the field (possible when scoring a promoted/appended tail) stay clamped at 0.
        assertThat(ScoringService.racenetDefaultPoints(50, 20)).isZero();
    }
}
