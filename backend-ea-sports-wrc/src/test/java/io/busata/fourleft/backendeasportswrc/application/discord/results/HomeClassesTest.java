package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.application.discord.results.HomeClasses.EventEntries;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.common.ClassLockMode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HomeClassesTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 26, 12, 0);

    private final Map<ClubLeaderboardEntry, String> clubs = new IdentityHashMap<>();

    @Test
    void theHomeIsTheClassOfTheFirstRunInTheChampionship() {
        ClubLeaderboardEntry finlandWrc2 = entry("a", "wrc2", T0);
        ClubLeaderboardEntry swedenWrc = entry("a", "wrc", T0.plusDays(7));

        HomeClasses homes = HomeClasses.of(ClassLockMode.WARN, List.of(event(finlandWrc2), event(swedenWrc)));

        assertThat(homes.homeOf(swedenWrc)).isEqualTo("wrc2");
        assertThat(homes.isOffClass("wrc", swedenWrc)).isTrue();
        assertThat(homes.isOffClass("wrc2", finlandWrc2)).isFalse();
        assertThat(homes.lockedOut("wrc")).containsExactly("a");
        assertThat(homes.lockedOut("wrc2")).isEmpty();
    }

    @Test
    void withinTheFirstEventTheFirstSeenRunDecides() {
        ClubLeaderboardEntry wrc = entry("a", "wrc", T0.plusMinutes(30));
        ClubLeaderboardEntry wrc2 = entry("a", "wrc2", T0);

        assertThat(HomeClasses.of(ClassLockMode.WARN, List.of(event(wrc, wrc2))).homeOf(wrc)).isEqualTo("wrc2");
    }

    @Test
    void theHomeRunIsKeptEvenWhenAnotherClassWasSeenFirst() {
        HomeClasses homes = HomeClasses.of(ClassLockMode.WARN, List.of(event(entry("a", "wrc", T0))));
        ClubLeaderboardEntry offClass = entry("a", "wrc2", T0.plusDays(7));
        ClubLeaderboardEntry home = entry("a", "wrc", T0.plusDays(8));

        assertThat(homes.select(List.of(home, offClass), clubs::get)).containsExactly(home);
    }

    @Test
    void anOffClassOnlyRunIsFlaggedUnderWarnAndHiddenUnderExclude() {
        List<EventEntries> history = List.of(event(entry("a", "wrc", T0)));
        ClubLeaderboardEntry offClass = entry("a", "wrc2", T0.plusDays(7));
        ClubLeaderboardEntry other = entry("b", "wrc2", T0.plusDays(7));

        HomeClasses warn = HomeClasses.of(ClassLockMode.WARN, history);
        assertThat(warn.select(List.of(offClass, other), clubs::get)).containsExactly(offClass, other);
        assertThat(warn.isOffClass("wrc2", offClass)).isTrue();

        assertThat(HomeClasses.of(ClassLockMode.EXCLUDE, history).select(List.of(offClass, other), clubs::get)).containsExactly(other);
    }

    @Test
    void offKeepsFirstRunsAndLocksNobody() {
        ClubLeaderboardEntry first = entry("a", "wrc2", T0);
        ClubLeaderboardEntry later = entry("a", "wrc", T0.plusMinutes(5));
        HomeClasses off = HomeClasses.of(ClassLockMode.OFF, List.of(event(entry("a", "wrc", T0.minusDays(7)))));

        assertThat(off.select(List.of(later, first), clubs::get)).containsExactly(first);
        assertThat(off.lockedOut("wrc2")).isEmpty();
    }

    @Test
    void laterEventsOnlyAddDriversNotSeenBefore() {
        HomeClasses homes = HomeClasses.of(ClassLockMode.WARN, List.of(event(entry("a", "wrc", T0))))
                .then(List.of(event(entry("a", "wrc2", T0.plusDays(7)), entry("b", "wrc2", T0.plusDays(7)))));

        assertThat(homes.lockedOut("wrc2")).containsExactly("a");
        assertThat(homes.lockedOut("wrc")).containsExactly("b");
    }

    private EventEntries event(ClubLeaderboardEntry... entries) {
        Map<ClubLeaderboardEntry, String> eventClubs = new IdentityHashMap<>();
        for (ClubLeaderboardEntry entry : entries) {
            eventClubs.put(entry, clubs.get(entry));
        }
        return new EventEntries(new ArrayList<>(List.of(entries)), eventClubs);
    }

    private ClubLeaderboardEntry entry(String ssid, String clubId, LocalDateTime firstSeen) {
        ClubLeaderboardEntry entry = ClubLeaderboardEntry.builder().ssid(ssid).displayName(ssid).build();
        entry.setFirstSeenAt(firstSeen);
        clubs.put(entry, clubId);
        return entry;
    }
}
