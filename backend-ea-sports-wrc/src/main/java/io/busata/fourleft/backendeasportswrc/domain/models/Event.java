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

    public boolean isActiveNow() {
        var now = ApplicationClock.now();

        return now.isAfter(absoluteOpenDate.toLocalDateTime()) && now.isBefore(absoluteCloseDate.toLocalDateTime());
    }

    public boolean requiresLeaderboardUpdate() {
        boolean updatedAfterClose = this.lastLeaderboardUpdate.isAfter(this.absoluteCloseDate.toLocalDateTime());

        boolean timeSinceLastUpdate = Duration.between(lastLeaderboardUpdate, ApplicationClock.now()).toMinutes() >= 10;


        return !updatedAfterClose && timeSinceLastUpdate;
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
}
