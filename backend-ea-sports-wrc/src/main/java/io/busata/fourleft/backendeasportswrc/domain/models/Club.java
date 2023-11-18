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
        boolean refreshLimitReached = Duration.between(lastDetailsUpdate, ApplicationClock.now()).toHours() >= 24;

        return refreshLimitReached || updateDetailsRequired;
    }

    public void markUpdated() {
        this.lastDetailsUpdate = ApplicationClock.now();
        this.updateDetailsRequired = false;
    }

    public Optional<Championship> getActiveChampionshipSnapshot() {
        return this.getChampionships().stream().filter(Championship::isActiveSnapshot).findFirst();
    }

}
