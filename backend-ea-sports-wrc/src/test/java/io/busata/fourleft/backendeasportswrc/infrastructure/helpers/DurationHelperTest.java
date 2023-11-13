package io.busata.fourleft.backendeasportswrc.infrastructure.helpers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DurationHelperTest {

    @Test
    public void testDurationParsing() {
        Duration duration = DurationHelper.parseDuration("00:00:01.5200000");

        assertThat(duration).hasMillis(1520);
    }

    @Test
    public void testDurationParsingZeroTime() {
        Duration duration = DurationHelper.parseDuration("00:00:00");

        assertThat(duration).hasMillis(0);
    }

    @Test
    public void testDurationParsingHours() {
        Duration duration = DurationHelper.parseDuration("01:11:15.3020000");

        assertThat(duration).hasMinutes(71);
    }
    @Test
    public void testDurationParsingMinutes() {
        Duration duration = DurationHelper.parseDuration("00:26:34.4100000");
        assertThat(duration).hasMinutes(26);
    }

    @Test
    public void testFormatting() {

        Duration duration = DurationHelper.parseDuration("00:05:31.7770000");

        log.info(DurationHelper.formatTime(duration));


    }
}