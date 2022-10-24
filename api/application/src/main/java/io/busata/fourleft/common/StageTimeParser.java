package io.busata.fourleft.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class StageTimeParser {

    public String getDiff(String fastestTime, String actualTime) {
        final var fastestDuration = createDuration(fastestTime);
        final var actualDuration = createDuration(actualTime);

        Duration difference = actualDuration.minus(fastestDuration);
        if(difference.isZero()) {
            return "--";
        }

        if(difference.toHours() > 1) {
            return DurationFormatUtils.formatDuration(difference.toMillis(), "+HH:mm:ss.SSS", true);
        } else {

            return DurationFormatUtils.formatDuration(difference.toMillis(), "+mm:ss.SSS", true);
        }
    }
    public Duration createDuration(String stageTime) {

        String[] splitted = stageTime.split(":");
        final var components = splitted.length;
        return switch (components) {
            case 2 -> parseMinutesAndSeconds(splitted);
            case 3 -> parseHoursMinutesAndSeconds(splitted);
            default -> throw new RuntimeException("Not supported");
        };
    }

    private Duration parseMinutesAndSeconds(String[] components) {
        String[] secondsAndMilliseconds = components[1].split("\\.");


        long minutes = Long.parseLong(components[0]);
        long seconds = Long.parseLong(secondsAndMilliseconds[0]);
        long milliseconds = Long.parseLong(secondsAndMilliseconds[1]);



        return Duration.ofMinutes(minutes).plusSeconds(seconds).plusMillis(milliseconds);
    }

    private Duration parseHoursMinutesAndSeconds(String[] components) {
        String[] secondsAndMilliseconds = components[2].split("\\.");

        long hours = Long.parseLong(components[0]);
        long minutes = Long.parseLong(components[1]);
        long seconds = Long.parseLong(secondsAndMilliseconds[0]);
        long milliseconds = Long.parseLong(secondsAndMilliseconds[1]);


        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds).plusMillis(milliseconds);
    }
}
