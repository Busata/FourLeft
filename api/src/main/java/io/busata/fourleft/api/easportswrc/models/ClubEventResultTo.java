package io.busata.fourleft.api.easportswrc.models;

import java.time.ZonedDateTime;
import java.util.List;

public record ClubEventResultTo(
        EventSettingsTo eventSettingsTo, List<StageSettingsTo> list,
        String id,
        String status,
        ZonedDateTime absoluteOpenDate,
        ZonedDateTime absoluteCloseDate,
        List<ClubResultEntryTo> clubResultEntryTos) {
}
