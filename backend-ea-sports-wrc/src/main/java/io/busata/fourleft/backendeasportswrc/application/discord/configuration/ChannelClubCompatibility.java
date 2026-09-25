package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Whether a multi-club channel's clubs run the same championship, so their results can be merged.
 * {@code problems} is empty when compatible; {@code clubs} describes what each club is running, in
 * channel order, for the configuration page.
 */
public record ChannelClubCompatibility(boolean compatible, List<String> problems, List<ClubChampionship> clubs) {

    public record ClubChampionship(String clubId,
                                   String label,
                                   String championshipId,
                                   String championshipName,
                                   String location,
                                   String vehicleClass,
                                   ZonedDateTime eventCloseDate) {
    }
}
