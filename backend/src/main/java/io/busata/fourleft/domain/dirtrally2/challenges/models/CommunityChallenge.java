package io.busata.fourleft.domain.dirtrally2.challenges.models;

import io.busata.fourleft.common.DR2CommunityEventType;
import io.busata.fourleft.common.LeaderboardKey;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name="community_challenge")
public class CommunityChallenge {

    @Id
    @GeneratedValue
    UUID id;

    @Enumerated(EnumType.STRING)
    DR2CommunityEventType type;

    String challengeId;
    String vehicleClass;
    boolean isDLC;

    ZonedDateTime startTime;
    ZonedDateTime endTime;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eventId asc")
    List<CommunityEvent> events = new ArrayList<>();

    public void updateEvents(List<CommunityEvent> events) {
        this.events.clear();;
        this.events.addAll(events);
    }

    boolean synced;
    boolean ended;

    public Optional<LeaderboardKey> getLeaderboardKey() {
        return getLastEvent().flatMap(event -> {
            return event.getLastStage().flatMap(stage -> {
                LeaderboardKey leaderboardKey = new LeaderboardKey(challengeId, event.getEventId(), stage.getStageId());
                return Optional.of(leaderboardKey);
            });
        });
    }

    public Optional<CommunityEvent> getLastEvent() {
        if(events.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(events.get(events.size() -1));
        }
    }
}
