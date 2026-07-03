package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Entity
@Getter
@NoArgsConstructor
public class Club {

    @Id
    String id;

    String clubName;
    String clubDescription;

    Long activeMemberCount;

    ZonedDateTime clubCreatedAt;

    boolean updateDetailsRequired;

    @OneToMany(mappedBy="club", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Championship> championships = new ArrayList<>();

    LocalDateTime lastDetailsUpdate;
    LocalDateTime lastLeaderboardUpdate;

    public Club(String id, String clubName, String clubDescription, Long activeMemberCount, ZonedDateTime clubCreatedAt) {
        this.id = id;
        this.clubName = clubName;
        this.clubDescription = clubDescription;
        this.activeMemberCount = activeMemberCount;
        this.clubCreatedAt = clubCreatedAt;
        this.lastDetailsUpdate = ApplicationClock.now();
        this.lastLeaderboardUpdate = ApplicationClock.now().minusYears(1);
    }

    public void updateBasicDetails(String clubName, String clubDescription, Long activeMemberCount) {
        this.clubName = clubName;
        this.clubDescription = clubDescription;
        this.activeMemberCount = activeMemberCount;
    }


    public void updateChampionship(Championship championship) {
        if (!this.championships.contains(championship)) {
            championship.setClub(this);
            this.championships.add(championship);
        }
    }

    public List<String> getLeadersboardsRequiringUpdate() {
        return this.getActiveChampionshipSnapshot()
                .stream()
                .map(Championship::getEvents).flatMap(Collection::stream)
                .filter(Event::requiresLeaderboardUpdate)
                .map(Event::getLeaderboardIds).flatMap(Collection::stream)
                .toList();

    }

    public List<String> getOpenLeaderboards() {
        return this.getActiveChampionshipSnapshot()
                .flatMap(Championship::getActiveEventSnapshot)
                .map(Event::getLeaderboardIds)
                .orElse(List.of());
    }

    /*
        Leaderboards from championships that are already finished but were never fetched.
     */
    public Set<String> getHistoryLeaderboards() {
        return this.getChampionships().stream()
                .filter(championship -> championship.getStatus() == EventStatus.FINISHED)
                .filter(championship -> !championship.isUpdatedAfterFinish())
                .map(Championship::getEvents)
                .flatMap(Collection::stream)
                .map(Event::getLeaderboardIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public boolean requiresDetailsUpdate() {
        int updateTime = this.getActiveChampionshipSnapshot().map(activeChampionship -> 24).orElse(1);
        boolean refreshLimitReached = Duration.between(lastDetailsUpdate, ApplicationClock.now()).toHours() >= updateTime;

        return refreshLimitReached || updateDetailsRequired;
    }

    public void markUpdated() {
        this.lastDetailsUpdate = ApplicationClock.now();
        this.updateDetailsRequired = false;
    }

    /**
     * Earliest FUTURE moment this club needs importing again — the temporal inverse of the
     * {@code requiresX} predicates. Used by the work queue to schedule the next run instead of a
     * fixed cadence. Returns empty when nothing is scheduled ahead (the caller applies a horizon).
     * A club that is <i>already</i> due is decided by {@code requiresImport}, not here.
     */
    public Optional<LocalDateTime> nextImportDueAt() {
        LocalDateTime now = ApplicationClock.now();

        // 1. Detail refresh cadence: 24h while a championship is active, else 1h (mirrors requiresDetailsUpdate).
        int detailHours = getActiveChampionshipSnapshot().map(activeChampionship -> 24).orElse(1);
        Stream<LocalDateTime> detailDue = Stream.of(lastDetailsUpdate.plusHours(detailHours));

        // 2. Leaderboard refresh: every 10min for the active championship's events not yet closed-out
        //    (mirrors Event.requiresLeaderboardUpdate).
        Stream<LocalDateTime> leaderboardDue = getActiveChampionshipSnapshot().stream()
                .flatMap(championship -> championship.getEvents().stream())
                .filter(event -> !event.getLastLeaderboardUpdate().isAfter(event.getAbsoluteCloseDate().toLocalDateTime()))
                .map(event -> event.getLastLeaderboardUpdate().plusMinutes(10));

        // 3. Event/championship boundaries: an open date flips a championship active; a close date
        //    finishes it (event-ended / history). Any future one is a moment work can become due.
        Stream<LocalDateTime> boundaries = getChampionships().stream().flatMap(championship -> Stream.concat(
                Stream.of(championship.getAbsoluteOpenDate().toLocalDateTime(), championship.getAbsoluteCloseDate().toLocalDateTime()),
                championship.getEvents().stream().flatMap(event -> Stream.of(
                        event.getAbsoluteOpenDate().toLocalDateTime(), event.getAbsoluteCloseDate().toLocalDateTime()))));

        return Stream.of(detailDue, leaderboardDue, boundaries)
                .flatMap(stream -> stream)
                .filter(candidate -> candidate.isAfter(now))
                .min(Comparator.naturalOrder());
    }

    public Optional<Championship> getActiveChampionshipSnapshot() {
        return this.getChampionships().stream().filter(Championship::isActiveSnapshot).findFirst();
    }

    public Optional<Championship> getUpcomingChampionshipSnapshot() {
        return this.getChampionships().stream().filter(Championship::isUpcomingSnapshot).findFirst();
    }

}
