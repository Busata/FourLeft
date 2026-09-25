package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.application.discord.configuration.ChannelClubCompatibility.ClubChampionship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChannelClub;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.common.ChannelClubMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Decides whether a MIXED channel's clubs can be merged: every club must be running the same
 * championship as the primary — same number of events, and per event the same location, season,
 * stages (route + weather/surface) and open/close window. Only the car class may differ.
 *
 * <p>The compared championship is the club's active one, else its upcoming one, else its most recent,
 * so a channel stays merged between championships and while the final event's results are posted.
 */
@Service
@RequiredArgsConstructor
public class ChannelClubCompatibilityService {

    // Clubs created by hand for the same series won't always open/close to the second; the one real
    // pair seen so far did, so this is only slack for manual setup, not for genuinely different events.
    static final Duration SCHEDULE_TOLERANCE = Duration.ofHours(1);

    private final ClubService clubService;

    /** MIXED only when configured, with at least two clubs, and those clubs are compatible — SINGLE otherwise. */
    @Transactional(readOnly = true)
    public ChannelClubMode effectiveMode(DiscordClubConfiguration configuration) {
        if (configuration.getMode() != ChannelClubMode.MIXED || configuration.getClubs().size() < 2) {
            return ChannelClubMode.SINGLE;
        }
        return evaluate(configuration).compatible() ? ChannelClubMode.MIXED : ChannelClubMode.SINGLE;
    }

    @Transactional(readOnly = true)
    public ChannelClubCompatibility evaluate(DiscordClubConfiguration configuration) {
        List<String> problems = new ArrayList<>();
        List<ClubChampionship> described = new ArrayList<>();
        List<Optional<Championship>> championships = new ArrayList<>();

        if (configuration.getClubs().size() < 2) {
            problems.add("Mixed mode needs a second club.");
        }

        for (ChannelClub channelClub : configuration.getClubs()) {
            Optional<Club> club = findClub(channelClub.getClubId());
            Optional<Championship> championship = club.flatMap(this::currentChampionship);
            championships.add(championship);
            described.add(describe(channelClub, championship));

            if (club.isEmpty()) {
                problems.add("Club %s hasn't been synced yet.".formatted(channelClub.getClubId()));
            } else if (championship.isEmpty()) {
                problems.add("Club %s has no championship.".formatted(channelClub.getClubId()));
            }
        }

        if (problems.isEmpty()) {
            Championship primary = championships.get(0).orElseThrow();
            for (int i = 1; i < championships.size(); i++) {
                compare(primary, championships.get(i).orElseThrow(), configuration.getClubs().get(i).getClubId(), problems);
            }
        }

        return new ChannelClubCompatibility(problems.isEmpty(), problems, described);
    }

    /**
     * The club's event that runs alongside {@code reference} (another club's event): same location, closing
     * within the schedule tolerance. Searched across all the club's championships, so it doesn't depend on
     * how far either club's sync has progressed.
     */
    @Transactional(readOnly = true)
    public Optional<Event> matchingEvent(String clubId, Event reference) {
        return findClub(clubId).stream()
                .flatMap(club -> club.getChampionships().stream())
                .flatMap(championship -> championship.getEvents().stream())
                .filter(event -> Objects.equals(event.getEventSettings().getLocationID(), reference.getEventSettings().getLocationID()))
                .filter(event -> Duration.between(event.getAbsoluteCloseDate(), reference.getAbsoluteCloseDate()).abs().compareTo(SCHEDULE_TOLERANCE) <= 0)
                .findFirst();
    }

    /** The club's championship that runs alongside {@code reference}: opening and closing within the tolerance. */
    @Transactional(readOnly = true)
    public Optional<Championship> matchingChampionship(String clubId, Championship reference) {
        return findClub(clubId).stream()
                .flatMap(club -> club.getChampionships().stream())
                .filter(championship -> Duration.between(championship.getAbsoluteOpenDate(), reference.getAbsoluteOpenDate()).abs().compareTo(SCHEDULE_TOLERANCE) <= 0)
                .filter(championship -> Duration.between(championship.getAbsoluteCloseDate(), reference.getAbsoluteCloseDate()).abs().compareTo(SCHEDULE_TOLERANCE) <= 0)
                .findFirst();
    }

    private void compare(Championship primary, Championship other, String otherClubId, List<String> problems) {
        List<Event> primaryEvents = sortedEvents(primary);
        List<Event> otherEvents = sortedEvents(other);

        if (primaryEvents.size() != otherEvents.size()) {
            problems.add("Club %s's championship has %d events, the primary club's has %d."
                    .formatted(otherClubId, otherEvents.size(), primaryEvents.size()));
            return;
        }

        for (int i = 0; i < primaryEvents.size(); i++) {
            Event expected = primaryEvents.get(i);
            Event actual = otherEvents.get(i);
            String where = "Club %s, event %d (%s)".formatted(otherClubId, i + 1, expected.getEventSettings().getLocation());

            if (!Objects.equals(expected.getEventSettings().getLocationID(), actual.getEventSettings().getLocationID())) {
                problems.add("%s: location is %s.".formatted(where, actual.getEventSettings().getLocation()));
                continue;
            }
            if (!Objects.equals(expected.getEventSettings().getWeatherSeasonID(), actual.getEventSettings().getWeatherSeasonID())) {
                problems.add("%s: season is %s instead of %s.".formatted(where, actual.getEventSettings().getWeatherSeason(), expected.getEventSettings().getWeatherSeason()));
            }
            if (!stageKeys(expected).equals(stageKeys(actual))) {
                problems.add("%s: the stages or their conditions differ.".formatted(where));
            }
            if (!withinTolerance(expected, actual)) {
                problems.add("%s: opens/closes at a different time.".formatted(where));
            }
        }
    }

    private boolean withinTolerance(Event expected, Event actual) {
        return Duration.between(expected.getAbsoluteOpenDate(), actual.getAbsoluteOpenDate()).abs().compareTo(SCHEDULE_TOLERANCE) <= 0
                && Duration.between(expected.getAbsoluteCloseDate(), actual.getAbsoluteCloseDate()).abs().compareTo(SCHEDULE_TOLERANCE) <= 0;
    }

    // Stage rows carry no order column, so they're compared as a sorted list rather than trusting load order.
    private List<String> stageKeys(Event event) {
        return event.getStages().stream()
                .map(Stage::getStageSettings)
                .map(settings -> settings.getRouteID() + ":" + settings.getWeatherAndSurfaceID())
                .sorted()
                .toList();
    }

    private List<Event> sortedEvents(Championship championship) {
        return championship.getEvents().stream().sorted(Comparator.comparing(Event::getAbsoluteOpenDate)).toList();
    }

    private Optional<Club> findClub(String clubId) {
        return clubService.exists(clubId) ? Optional.of(clubService.findById(clubId)) : Optional.empty();
    }

    private Optional<Championship> currentChampionship(Club club) {
        return club.getActiveChampionshipSnapshot()
                .or(club::getUpcomingChampionshipSnapshot)
                .or(() -> clubService.getPreviousChampionship(club));
    }

    private ClubChampionship describe(ChannelClub channelClub, Optional<Championship> championship) {
        Optional<Event> event = championship.flatMap(ch -> ch.getActiveEventSnapshot()
                .or(() -> sortedEvents(ch).stream().reduce((first, second) -> second)));
        return new ClubChampionship(
                channelClub.getClubId(),
                channelClub.getLabel(),
                championship.map(Championship::getId).orElse(null),
                championship.map(ch -> ch.getSettings().getName()).orElse(null),
                event.map(e -> e.getEventSettings().getLocation()).orElse(null),
                event.map(e -> e.getEventSettings().getVehicleClass()).orElse(null),
                event.map(Event::getAbsoluteCloseDate).orElse(null));
    }
}
