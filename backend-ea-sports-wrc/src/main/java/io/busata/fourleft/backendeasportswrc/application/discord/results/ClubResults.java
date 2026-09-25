package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * One event's results. For a MIXED channel the entries of every club are merged: {@code classes} lists the
 * clubs in channel order and {@code entryClubs} (identity-keyed — the same driver can enter two classes)
 * tells which club each entry came from. The ids and event settings are the primary club's.
 */
public record ClubResults(
        String clubId,
        String championshipId,
        String eventId,
        String championshipName,
        String location,
        Long locationID,
        Long lastStageRouteID,
        String vehicleClass,
        Long vehicleClassID,
        String weatherSeason,
        Long weatherSeasonID,
        String lastStageWeatherAndSurface,
        Long lastStageWeatherAndSurfaceId,
        LocalDateTime lastUpdated,
        ZonedDateTime eventCloseDate,
        List<String> stages,
        List<ClubLeaderboardEntry> entries,
        List<ChannelClass> classes,
        Map<ClubLeaderboardEntry, String> entryClubs
) {

    public ClubResults(String clubId, String championshipId, String eventId, String championshipName, String location,
                       Long locationID, Long lastStageRouteID, String vehicleClass, Long vehicleClassID,
                       String weatherSeason, Long weatherSeasonID, String lastStageWeatherAndSurface,
                       Long lastStageWeatherAndSurfaceId, LocalDateTime lastUpdated, ZonedDateTime eventCloseDate,
                       List<String> stages, List<ClubLeaderboardEntry> entries) {
        this(clubId, championshipId, eventId, championshipName, location, locationID, lastStageRouteID, vehicleClass,
                vehicleClassID, weatherSeason, weatherSeasonID, lastStageWeatherAndSurface, lastStageWeatherAndSurfaceId,
                lastUpdated, eventCloseDate, stages, entries, List.of(), new IdentityHashMap<>());
    }

    public boolean isMixed() {
        return !classes.isEmpty();
    }

    /** The class tag of the entry's club; null for single-club results. */
    public String tagOf(ClubLeaderboardEntry entry) {
        String entryClub = entryClubs.get(entry);
        return classes.stream().filter(c -> c.clubId().equals(entryClub)).map(ChannelClass::tag).findFirst().orElse(null);
    }

    /** Event restrictions target the primary club's championship, so only its entries can violate one. */
    public boolean isPrimaryEntry(ClubLeaderboardEntry entry) {
        return !isMixed() || clubId.equals(entryClubs.get(entry));
    }
}
