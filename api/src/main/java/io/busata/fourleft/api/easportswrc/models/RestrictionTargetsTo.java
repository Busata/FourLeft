package io.busata.fourleft.api.easportswrc.models;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * The championships/events of a channel's club that a restriction rule can target, for the config UI pickers.
 */
public record RestrictionTargetsTo(List<RestrictionTargetChampionshipTo> championships) {

    public record RestrictionTargetChampionshipTo(
            String id,
            String name,
            ZonedDateTime absoluteOpenDate,
            ZonedDateTime absoluteCloseDate,
            List<RestrictionTargetEventTo> events)
    {
    }

    public record RestrictionTargetEventTo(
            String id,
            String location,
            String vehicleClass,
            ZonedDateTime absoluteCloseDate)
    {
    }
}
