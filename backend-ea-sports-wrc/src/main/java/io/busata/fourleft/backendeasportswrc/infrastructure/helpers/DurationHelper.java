package io.busata.fourleft.backendeasportswrc.infrastructure.helpers;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DurationHelper {

    public static Duration parseDuration(String durationAsString) {
        String[] parts = durationAsString.split(":");

        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);

        String[] secondParts = parts[2].split("\\.");

        long seconds = Long.parseLong(secondParts[0]);


        Duration duration = Duration.ofHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);


        if(secondParts.length > 1) {
            long nanos = Long.parseLong(secondParts[1]);


            duration = duration.plusNanos(nanos * 100);
        }

        return duration;
    }

    public static String formatTime(Duration duration) {
        if(duration.toHoursPart() == 0) {
            return DurationFormatUtils.formatDuration(duration.toMillis(), "mm:ss.SSS");
        } else {
            return DurationFormatUtils.formatDuration(duration.toMillis(), "HH:mm:ss.SSS");
        }
    }

    public static String formatDelta(Duration duration) {
        if(duration.toMillis() == 0) {
            return "--";
        } else {
            return "+" + formatTime(duration);
        }
    }
}
