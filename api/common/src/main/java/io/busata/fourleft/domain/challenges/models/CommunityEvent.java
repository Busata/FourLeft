package io.busata.fourleft.domain.challenges.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Setter
@Getter
public class CommunityEvent {
    @Id
    @GeneratedValue
    UUID id;

    String eventId;

    String name;
    String discipline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="challenge_id")
    private CommunityChallenge challenge;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stageId asc")
    List<CommunityStage> stages = new ArrayList<>();


    public void updateStages(List<CommunityStage> stages) {
        this.stages.clear();;
        this.stages.addAll(stages);
    }

    public Optional<CommunityStage> getLastStage() {
        if(stages.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(stages.get(stages.size() -1));
        }
    }
}
