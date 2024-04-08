package io.busata.fourleft.api.easportswrc.models;

import java.time.Duration;

public record ClubResultEntryTo(String wrcPlayerId, Long nationalityID, Long platform, Long rank, String vehicle, Duration time, Duration timeAccumulated, Duration timePenalty, Duration differenceToFirst, Duration differenceAccumulated) {


}
