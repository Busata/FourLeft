package io.busata.fourleft.backendeasportswrc.domain.models;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Getter
public class Event {

    @Id
    String id;

    String leaderboardId;

    ZonedDateTime absoluteOpenDate;
    ZonedDateTime absoluteCloseDate;

    LocalDateTime lastLeaderboardUpdate;

    @Column(name="championship_id")
    String championshipID;


    @Enumerated(EnumType.STRING)
    EventStatus status;

    @Embedded()
    EventSettings eventSettings;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    List<Stage> stages = new ArrayList<>();


    public Event(String id, String leaderboardId, ZonedDateTime absoluteOpenDate, ZonedDateTime absoluteCloseDate, Long status, EventSettings eventSettings) {
        this.id = id;
        this.leaderboardId = leaderboardId;
        this.absoluteOpenDate = absoluteOpenDate;
        this.absoluteCloseDate = absoluteCloseDate;
        this.status = EventStatus.fromValue(status);
        this.eventSettings = eventSettings;
        this.lastLeaderboardUpdate = ApplicationClock.now().minusYears(1);
    }

    public boolean isActiveSnapshot() {
        return this.status == EventStatus.OPEN;
    }

    public boolean isFinished() {
        return this.status == EventStatus.FINISHED;
    }

    public boolean isActiveNow() {
        var now = ApplicationClock.now();

        return now.isAfter(absoluteOpenDate.toLocalDateTime()) && now.isBefore(absoluteCloseDate.toLocalDateTime());
    }

    public boolean requiresLeaderboardUpdate() {
        LocalDateTime now = ApplicationClock.now();

        // Only events that have actually started refresh. Without this guard an upcoming event's
        // close date is always in the future, so updatedAfterClose can never become true and it
        // re-qualifies every 10 minutes forever (stamping itself with a time still before its close),
        // dragging every not-yet-started event of the championship into the update set.
        boolean hasOpened = !now.isBefore(this.absoluteOpenDate.toLocalDateTime());

        boolean updatedAfterClose = this.lastLeaderboardUpdate.isAfter(this.absoluteCloseDate.toLocalDateTime());

        boolean timeSinceLastUpdate = Duration.between(lastLeaderboardUpdate, now).toMinutes() >= 10;

        return hasOpened && !updatedAfterClose && timeSinceLastUpdate;
    }

    public Stage getLastStage() {
        return this.stages.get(this.stages.size() - 1);
    }

    public List<String> getLeaderboardIds() {
        List<String> leaderboardIds = getStages().stream().map(Stage::getLeaderboardId).collect(Collectors.toList());
        leaderboardIds.add(0, this.leaderboardId);
        return leaderboardIds;
    }


    public void updateStages(List<Stage> stages) {
        this.stages.clear();
        this.stages.addAll(stages);
    }

    public void updateStatus() {
        var now = ApplicationClock.now();

        if(now.isBefore(absoluteOpenDate.toLocalDateTime())) {
            this.status = EventStatus.NOT_STARTED;
            return;
        }

        if(now.isAfter(absoluteCloseDate.toLocalDateTime())) {
            this.status = EventStatus.FINISHED;
            return;
        }

        this.status = EventStatus.OPEN;
    }

    public void markClosed() {
        this.status = EventStatus.FINISHED;
    }
}
