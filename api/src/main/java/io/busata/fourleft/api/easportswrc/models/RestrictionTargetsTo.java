package io.busata.fourleft.api.easportswrc.models;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * The championships/events of a channel's clubs that a restriction rule can target, for the config UI pickers.
 * Ids are per club, so a rule on a championship only ever applies to the club that owns it; {@code clubTag}
 * (the club's label, else its car class) tells the clubs of a multi-club channel apart.
 */
public record RestrictionTargetsTo(List<RestrictionTargetChampionshipTo> championships) {

    public record RestrictionTargetChampionshipTo(
            String clubId,
            String clubTag,
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
