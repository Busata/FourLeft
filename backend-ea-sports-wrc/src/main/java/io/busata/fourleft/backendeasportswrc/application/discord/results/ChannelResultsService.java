package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.application.discord.configuration.ChannelClubCompatibilityService;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChannelClub;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.EventStatus;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.common.ChannelClubMode;
import io.busata.fourleft.common.ClassLockMode;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private final ClubLeaderboardService clubLeaderboardService;

    /** Homes from a channel's finished events, which no longer change; reused while that set is the same. */
    private record CachedHomes(String signature, HomeClasses homes) {
    }

    private final Map<Long, CachedHomes> finishedHomes = new ConcurrentHashMap<>();

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
            return List.of(new StandingsSection(null, sortedStandings(configuration, configuration.getPrimaryClubId(), Set.of())));
        }

        // Racenet keeps one points table per club, so each class stays its own block within the post. A
        // driver only appears in their home class's block.
        HomeClasses homes = clubResultsService.standingsChampionship(clubService.findById(configuration.getPrimaryClubId()))
                .map(championship -> homeClasses(configuration, championship))
                .orElse(HomeClasses.none());
        Map<String, String> tags = currentTags(configuration);
        return configuration.getClubs().stream()
                .map(club -> new StandingsSection(tags.get(club.getClubId()),
                        sortedStandings(configuration, club.getClubId(), homes.lockedOut(club.getClubId()))))
                .filter(section -> !section.standings().isEmpty())
                .toList();
    }

    /** The home classes for the championship of one of the primary club's events. */
    @Transactional(readOnly = true)
    public HomeClasses homeClasses(DiscordClubConfiguration configuration, Event primaryEvent) {
        if (configuration.getClassLockMode() == ClassLockMode.OFF) {
            return HomeClasses.none();
        }
        return clubService.findById(configuration.getPrimaryClubId()).getChampionships().stream()
                .filter(championship -> championship.getId().equals(primaryEvent.getChampionshipID()))
                .findFirst()
                .map(championship -> homeClasses(configuration, championship))
                .orElse(HomeClasses.none());
    }

    /**
     * Every driver's home class in a MIXED channel's championship (the primary's), from all clubs' boards of
     * its events so far. Events whose every class has finished are cached per channel; only the running ones
     * are read on each call (autoposting asks on every sync).
     */
    @Transactional(readOnly = true)
    public HomeClasses homeClasses(DiscordClubConfiguration configuration, Championship primaryChampionship) {
        if (configuration.getClassLockMode() == ClassLockMode.OFF || !isMixed(configuration)) {
            return HomeClasses.none();
        }

        List<List<ClassEvent>> events = primaryChampionship.getEvents().stream()
                .filter(event -> event.getStatus() != EventStatus.NOT_STARTED)
                .sorted(Comparator.comparing(Event::getAbsoluteOpenDate))
                .map(event -> matchedClassEvents(configuration, event))
                .toList();

        // Finished events come first in running order; the cache covers that prefix.
        int finishedCount = 0;
        while (finishedCount < events.size() && events.get(finishedCount).stream().allMatch(classEvent -> classEvent.event().isFinished())) {
            finishedCount++;
        }
        List<List<ClassEvent>> finished = events.subList(0, finishedCount);
        List<List<ClassEvent>> running = events.subList(finishedCount, events.size());

        String signature = configuration.getClassLockMode() + "|" + finished.stream()
                .map(event -> event.stream().map(classEvent -> classEvent.channelClass().clubId() + ":" + classEvent.event().getId())
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining(";"));

        CachedHomes cached = finishedHomes.get(configuration.getChannelId());
        if (cached == null || !cached.signature().equals(signature)) {
            cached = new CachedHomes(signature, HomeClasses.of(configuration.getClassLockMode(), eventEntries(finished)));
            finishedHomes.put(configuration.getChannelId(), cached);
        }
        return cached.homes().then(eventEntries(running));
    }

    private List<HomeClasses.EventEntries> eventEntries(List<List<ClassEvent>> events) {
        Map<String, List<ClubLeaderboardEntry>> boards = clubLeaderboardService.findEntriesByLeaderboardIds(events.stream()
                .flatMap(List::stream)
                .map(classEvent -> classEvent.event().getLeaderboardId())
                .toList());

        return events.stream().map(event -> {
            List<ClubLeaderboardEntry> entries = new ArrayList<>();
            Map<ClubLeaderboardEntry, String> clubs = new IdentityHashMap<>();
            for (ClassEvent classEvent : event) {
                boards.getOrDefault(classEvent.event().getLeaderboardId(), List.of()).forEach(entry -> {
                    entries.add(entry);
                    clubs.put(entry, classEvent.channelClass().clubId());
                });
            }
            return new HomeClasses.EventEntries(entries, clubs);
        }).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ClubStats> getStats(DiscordClubConfiguration configuration) {
        String primaryClubId = configuration.getPrimaryClubId();
        if (!isMixed(configuration)) {
            return clubStatsService.buildStats(primaryClubId);
        }
        return clubService.findPreviousEvent(primaryClubId).flatMap(primaryEvent -> {
            List<ClassEvent> classEvents = matchedClassEvents(configuration, primaryEvent);
            List<Event> events = classEvents.stream().map(ClassEvent::event).toList();

            List<ClubLeaderboardEntry> allEntries = new ArrayList<>();
            Map<ClubLeaderboardEntry, String> entryClubs = new IdentityHashMap<>();
            for (ClassEvent classEvent : classEvents) {
                clubLeaderboardService.findEntries(classEvent.event().getLeaderboardId()).forEach(entry -> {
                    allEntries.add(entry);
                    entryClubs.put(entry, classEvent.channelClass().clubId());
                });
            }
            List<ClubLeaderboardEntry> entries = homeClasses(configuration, primaryEvent).select(allEntries, entryClubs::get);
            return clubStatsService.buildMergedStats(events, entries, joinedClasses(events));
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
                        classes.add(ChannelClass.of(club.getClubId(), club.getLabel(), results.vehicleClass(),
                                results.championshipId(), results.eventId()));
                    });
        }

        if (parts.isEmpty() || !parts.get(0).clubId().equals(configuration.getPrimaryClubId())) {
            return Optional.empty();
        }

        ClubResults primary = parts.get(0);
        List<ClubLeaderboardEntry> allEntries = new ArrayList<>();
        Map<ClubLeaderboardEntry, String> entryClubs = new IdentityHashMap<>();
        for (ClubResults part : parts) {
            part.entries().forEach(entry -> {
                allEntries.add(entry);
                entryClubs.put(entry, part.clubId());
            });
        }
        HomeClasses homes = homeClasses(configuration, primaryEvent);
        List<ClubLeaderboardEntry> entries = homes.select(allEntries, entryClubs::get);

        // Under WARN a run outside the driver's home class stays on the board, flagged with that home.
        Map<ClubLeaderboardEntry, String> offClassHomes = new IdentityHashMap<>();
        for (ClubLeaderboardEntry entry : entries) {
            if (homes.isOffClass(entryClubs.get(entry), entry)) {
                String home = homes.homeOf(entry);
                offClassHomes.put(entry, classes.stream().filter(c -> c.clubId().equals(home)).map(ChannelClass::tag).findFirst()
                        .orElseGet(() -> configuration.getClubs().stream().filter(club -> club.getClubId().equals(home))
                                .map(club -> ChannelClass.of(home, club.getLabel(), null).tag()).findFirst().orElse(home)));
            }
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
                entryClubs,
                offClassHomes
        ));
    }

    private List<ChampionshipStanding> sortedStandings(DiscordClubConfiguration configuration, String clubId, Set<String> lockedOut) {
        return clubResultsService.getStandings(configuration, clubId, lockedOut).stream()
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
