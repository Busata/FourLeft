package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.application.discord.configuration.ChannelClubCompatibilityService;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChannelClub;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.common.ChannelClubMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * What a channel shows, whatever its mode: the primary club's results for a SINGLE channel, every club's
 * merged for a MIXED one. The other clubs' events are matched to the primary's (same location and close
 * time) rather than taken as each club's own current event, so a club that syncs a bit later can't mix
 * two different events into one post.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelResultsService {

    private final ChannelClubCompatibilityService compatibilityService;
    private final ClubService clubService;
    private final ClubResultsService clubResultsService;
    private final ClubStatsService clubStatsService;

    /** A block of standings; {@code title} is the class tag in a MIXED channel, null otherwise. */
    public record StandingsSection(String title, List<ChampionshipStanding> standings) {
    }

    @Transactional(readOnly = true)
    public boolean isMixed(DiscordClubConfiguration configuration) {
        return compatibilityService.effectiveMode(configuration) == ChannelClubMode.MIXED;
    }

    @Transactional(readOnly = true)
    public Optional<ClubResults> getCurrentResults(DiscordClubConfiguration configuration) {
        String primaryClubId = configuration.getPrimaryClubId();
        if (!isMixed(configuration)) {
            return clubResultsService.getCurrentResults(primaryClubId);
        }
        return clubService.getActiveEvent(primaryClubId).flatMap(event -> mergedResults(configuration, event));
    }

    @Transactional(readOnly = true)
    public Optional<ClubResults> getPreviousResults(DiscordClubConfiguration configuration) {
        String primaryClubId = configuration.getPrimaryClubId();
        if (!isMixed(configuration)) {
            return clubResultsService.getPreviousResults(primaryClubId);
        }
        return clubService.findPreviousEvent(primaryClubId).flatMap(event -> mergedResults(configuration, event));
    }

    @Transactional(readOnly = true)
    public List<StandingsSection> getStandings(DiscordClubConfiguration configuration) {
        if (!isMixed(configuration)) {
            return List.of(new StandingsSection(null, sortedStandings(configuration, configuration.getPrimaryClubId())));
        }

        // Racenet keeps one points table per club, so each class stays its own block within the post.
        Map<String, String> tags = currentTags(configuration);
        return configuration.getClubs().stream()
                .map(club -> new StandingsSection(tags.get(club.getClubId()), sortedStandings(configuration, club.getClubId())))
                .filter(section -> !section.standings().isEmpty())
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ClubStats> getStats(DiscordClubConfiguration configuration) {
        String primaryClubId = configuration.getPrimaryClubId();
        if (!isMixed(configuration)) {
            return clubStatsService.buildStats(primaryClubId);
        }
        return clubService.findPreviousEvent(primaryClubId).flatMap(primaryEvent -> {
            List<Event> events = matchedEvents(configuration, primaryEvent);
            return clubStatsService.buildMergedStats(events, joinedClasses(events));
        });
    }

    /**
     * For a MIXED channel, the car classes of every club per primary-club event (e.g. "WRC / WRC2"), keyed by
     * the primary's event id — the championship summary shows those instead of the primary's class alone.
     */
    @Transactional(readOnly = true)
    public Map<String, String> summaryClasses(DiscordClubConfiguration configuration, Championship primaryChampionship) {
        if (!isMixed(configuration)) {
            return Map.of();
        }
        Map<String, String> classes = new LinkedHashMap<>();
        for (Event event : primaryChampionship.getEvents()) {
            classes.put(event.getId(), joinedClasses(matchedEvents(configuration, event)));
        }
        return classes;
    }

    /** A club's event in a MIXED channel with the class it shows as. */
    public record ClassEvent(ChannelClass channelClass, Event event) {
    }

    /**
     * Each club's event running alongside the primary's, in channel order (primary first); clubs without a
     * match (not synced yet) are left out.
     */
    @Transactional(readOnly = true)
    public List<ClassEvent> matchedClassEvents(DiscordClubConfiguration configuration, Event primaryEvent) {
        List<ClassEvent> events = new ArrayList<>();
        for (ChannelClub club : configuration.getClubs()) {
            Optional<Event> event = club.getClubId().equals(configuration.getPrimaryClubId())
                    ? Optional.of(primaryEvent)
                    : compatibilityService.matchingEvent(club.getClubId(), primaryEvent);
            event.ifPresentOrElse(
                    e -> events.add(new ClassEvent(ChannelClass.of(club.getClubId(), club.getLabel(), e.getEventSettings().getVehicleClass()), e)),
                    () -> log.warn("Club {} has no event matching {} of channel {}", club.getClubId(), primaryEvent.getId(), configuration.getChannelId()));
        }
        return events;
    }

    @Transactional(readOnly = true)
    public List<Event> matchedEvents(DiscordClubConfiguration configuration, Event primaryEvent) {
        return matchedClassEvents(configuration, primaryEvent).stream().map(ClassEvent::event).toList();
    }

    private Optional<ClubResults> mergedResults(DiscordClubConfiguration configuration, Event primaryEvent) {
        List<ChannelClass> classes = new ArrayList<>();
        List<ClubResults> parts = new ArrayList<>();

        for (ChannelClub club : configuration.getClubs()) {
            Optional<Event> event = club.getClubId().equals(configuration.getPrimaryClubId())
                    ? Optional.of(primaryEvent)
                    : compatibilityService.matchingEvent(club.getClubId(), primaryEvent);

            event.flatMap(e -> clubResultsService.getEventResults(club.getClubId(), e.getChampionshipID(), e.getId()))
                    .ifPresent(results -> {
                        parts.add(results);
                        classes.add(ChannelClass.of(club.getClubId(), club.getLabel(), results.vehicleClass()));
                    });
        }

        if (parts.isEmpty() || !parts.get(0).clubId().equals(configuration.getPrimaryClubId())) {
            return Optional.empty();
        }

        ClubResults primary = parts.get(0);
        List<ClubLeaderboardEntry> entries = new ArrayList<>();
        Map<ClubLeaderboardEntry, String> entryClubs = new IdentityHashMap<>();
        for (ClubResults part : parts) {
            part.entries().forEach(entry -> {
                entries.add(entry);
                entryClubs.put(entry, part.clubId());
            });
        }

        // The oldest update: the merged board is only as fresh as its stalest club.
        LocalDateTime lastUpdated = parts.stream().map(ClubResults::lastUpdated).filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(primary.lastUpdated());

        String vehicleClasses = classes.stream().map(ChannelClass::vehicleClass).filter(Objects::nonNull)
                .distinct().collect(Collectors.joining(" / "));

        return Optional.of(new ClubResults(
                primary.clubId(),
                primary.championshipId(),
                primary.eventId(),
                primary.championshipName(),
                primary.location(),
                primary.locationID(),
                primary.lastStageRouteID(),
                vehicleClasses,
                primary.vehicleClassID(),
                primary.weatherSeason(),
                primary.weatherSeasonID(),
                primary.lastStageWeatherAndSurface(),
                primary.lastStageWeatherAndSurfaceId(),
                lastUpdated,
                primary.eventCloseDate(),
                primary.stages(),
                entries,
                classes,
                entryClubs
        ));
    }

    private List<ChampionshipStanding> sortedStandings(DiscordClubConfiguration configuration, String clubId) {
        return clubResultsService.getStandings(configuration, clubId).stream()
                .sorted(Comparator.comparing(ChampionshipStanding::getRank))
                .toList();
    }

    // Tags from the clubs' current (else most recent) event classes, for section titles.
    private Map<String, String> currentTags(DiscordClubConfiguration configuration) {
        Map<String, String> tags = new LinkedHashMap<>();
        for (ChannelClub club : configuration.getClubs()) {
            String vehicleClass = clubService.getActiveEvent(club.getClubId())
                    .or(() -> clubService.findPreviousEvent(club.getClubId()))
                    .map(event -> event.getEventSettings().getVehicleClass())
                    .orElse(null);
            tags.put(club.getClubId(), ChannelClass.of(club.getClubId(), club.getLabel(), vehicleClass).tag());
        }
        return tags;
    }

    private static String joinedClasses(List<Event> events) {
        return events.stream().map(event -> event.getEventSettings().getVehicleClass()).filter(Objects::nonNull)
                .distinct().collect(Collectors.joining(" / "));
    }
}
