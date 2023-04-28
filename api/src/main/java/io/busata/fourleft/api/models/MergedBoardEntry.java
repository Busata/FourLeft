package io.busata.fourleft.api.models;

import java.time.Duration;

public record MergedBoardEntry(String key, Duration duration, int entriesDone, String rallyTime) {
}
