package io.busata.fourleft.api.easportswrc.models;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Whether a channel's clubs run the same championship (only the car class may differ). When they don't,
 * a MIXED channel falls back to SINGLE and only posts the primary club.
 */
public record ChannelClubCompatibilityTo(
        boolean compatible,
        List<String> problems,
        List<ClubChampionshipTo> clubs)
{
    public record ClubChampionshipTo(
            String clubId,
            String label,
            String championshipId,
            String championshipName,
            String location,
            String vehicleClass,
            ZonedDateTime eventCloseDate)
    {
    }
}
