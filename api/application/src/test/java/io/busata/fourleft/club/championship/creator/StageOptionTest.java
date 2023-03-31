package io.busata.fourleft.club.championship.creator;

import io.busata.fourleft.domain.options.models.StageOption;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class StageOptionTest {

    @Test
    public void printOutLongs() {
        final var longStages = Arrays.asList(StageOption.values()).stream()
                .filter(StageOption::isLong)
                .toList();

        assertThat(longStages.size()).isEqualTo(52);
    }
}