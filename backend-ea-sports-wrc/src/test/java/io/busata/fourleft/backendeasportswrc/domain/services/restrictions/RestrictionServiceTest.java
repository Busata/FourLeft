package io.busata.fourleft.backendeasportswrc.domain.services.restrictions;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.common.RestrictionDisplayMode;
import io.busata.fourleft.common.RestrictionScoringMode;
import io.busata.fourleft.common.RestrictionType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RestrictionServiceTest {

    private final RestrictionService service = new RestrictionService();

    private static EventRestriction championshipRule(String championshipId, List<String> vehicles) {
        return new EventRestriction(RestrictionType.VEHICLE_ALLOWLIST, championshipId, null,
                RestrictionDisplayMode.WARN, RestrictionScoringMode.EXCLUDE, null, vehicles);
    }

    private static EventRestriction eventRule(String eventId, List<String> vehicles) {
        return new EventRestriction(RestrictionType.VEHICLE_ALLOWLIST, null, eventId,
                RestrictionDisplayMode.WARN, RestrictionScoringMode.EXCLUDE, null, vehicles);
    }

    private static ClubLeaderboardEntry entry(String name, long rankAccumulated, String vehicle, boolean dnf) {
        Duration time = Duration.ofMinutes(3).plusSeconds(rankAccumulated);
        return ClubLeaderboardEntry.builder()
                .displayName(name)
                .ssid(name)
                .rank(rankAccumulated)
                .rankAccumulated(rankAccumulated)
                .nationalityID(1L)
                .vehicle(vehicle)
                .time(time)
                .timeAccumulated(time)
                // isDnf() is time == timePenalty, so a DNF carries its full time as penalty.
                .timePenalty(dnf ? time : Duration.ZERO)
                .build();
    }

    @Test
    void eventSpecificRuleWinsOverChampionshipRule() {
        EventRestriction championshipWide = championshipRule("champ-1", List.of("Audi"));
        EventRestriction eventSpecific = eventRule("event-2", List.of("Lancia"));

        Optional<EventRestriction> resolved = service.resolveRestriction(
                List.of(championshipWide, eventSpecific), "champ-1", "event-2");

        assertThat(resolved).contains(eventSpecific);
    }

    @Test
    void championshipRuleAppliesToAllItsEvents() {
        EventRestriction rule = championshipRule("champ-1", List.of("Audi"));

        assertThat(service.resolveRestriction(List.of(rule), "champ-1", "event-1")).contains(rule);
        assertThat(service.resolveRestriction(List.of(rule), "champ-1", "event-2")).contains(rule);
        assertThat(service.resolveRestriction(List.of(rule), "champ-other", "event-3")).isEmpty();
    }

    @Test
    void eventSpecificRuleOnlyMatchesItsEvent() {
        EventRestriction rule = eventRule("event-1", List.of("Audi"));

        assertThat(service.resolveRestriction(List.of(rule), "champ-1", "event-1")).contains(rule);
        assertThat(service.resolveRestriction(List.of(rule), "champ-1", "event-2")).isEmpty();
    }

    @Test
    void firstRuleWinsAmongDuplicateTargets() {
        // Save-side dedupe should prevent this, but resolution stays deterministic regardless.
        EventRestriction first = championshipRule("champ-1", List.of("Audi"));
        EventRestriction second = championshipRule("champ-1", List.of("Lancia"));

        assertThat(service.resolveRestriction(List.of(first, second), "champ-1", "event-1")).contains(first);
    }

    @Test
    void noRulesResolvesToEmpty() {
        assertThat(service.resolveRestriction(List.of(), "champ-1", "event-1")).isEmpty();
        assertThat(service.resolveRestriction((List<EventRestriction>) null, "champ-1", "event-1")).isEmpty();
    }

    @Test
    void violatesMatchesVehicleExactlyIgnoringSurroundingWhitespace() {
        EventRestriction rule = eventRule("event-1", List.of("Audi Sport quattro S1 E2 "));

        assertThat(service.violates(rule, entry("a", 1, "Audi Sport quattro S1 E2", false))).isFalse();
        assertThat(service.violates(rule, entry("b", 2, "Lancia Delta S4", false))).isTrue();
        assertThat(service.violates(rule, entry("c", 3, "Audi Sport quattro", false))).isTrue();
    }

    @Test
    void entryWithoutVehicleViolates() {
        EventRestriction rule = eventRule("event-1", List.of("Audi"));

        assertThat(service.violates(rule, entry("a", 1, null, false))).isTrue();
    }

    @Test
    void scoringPositionsSkipViolatorsAndKeepDnfSlots() {
        EventRestriction rule = eventRule("event-1", List.of("Audi"));

        ClubLeaderboardEntry violator = entry("violator", 1, "Lancia", false);
        ClubLeaderboardEntry dnf = entry("dnf", 2, "Audi", true);
        ClubLeaderboardEntry finisher = entry("finisher", 3, "Audi", false);

        Map<ClubLeaderboardEntry, Long> positions = service.scoringPositions(rule, List.of(violator, dnf, finisher));

        assertThat(positions).doesNotContainKey(violator);
        // The compliant DNF keeps occupying a slot, exactly as without a restriction.
        assertThat(positions).containsEntry(dnf, 1L);
        assertThat(positions).containsEntry(finisher, 2L);
    }
}
