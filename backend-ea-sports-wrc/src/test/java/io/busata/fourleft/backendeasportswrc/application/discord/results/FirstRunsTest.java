package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FirstRunsTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 26, 12, 0);

    @Test
    void keepsTheRunSeenFirst() {
        ClubLeaderboardEntry wrc = entry("a", T0.plusMinutes(30));
        ClubLeaderboardEntry wrc2 = entry("a", T0);
        ClubLeaderboardEntry other = entry("b", T0.plusMinutes(45));

        assertThat(FirstRuns.of(List.of(wrc, other, wrc2))).containsExactly(other, wrc2);
    }

    @Test
    void untrackedEntriesCountAsEarliest() {
        ClubLeaderboardEntry tracked = entry("a", T0);
        ClubLeaderboardEntry legacy = entry("a", null);

        assertThat(FirstRuns.of(List.of(tracked, legacy))).containsExactly(legacy);
    }

    @Test
    void tiesGoToChannelOrder() {
        ClubLeaderboardEntry primary = entry("a", T0);
        ClubLeaderboardEntry secondary = entry("a", T0);

        assertThat(FirstRuns.of(List.of(primary, secondary))).containsExactly(primary);
        assertThat(FirstRuns.of(List.of(entry("a", null), entry("a", null)))).hasSize(1);
    }

    private static ClubLeaderboardEntry entry(String ssid, LocalDateTime firstSeen) {
        ClubLeaderboardEntry entry = ClubLeaderboardEntry.builder().ssid(ssid).displayName(ssid).build();
        entry.setFirstSeenAt(firstSeen);
        return entry;
    }
}
