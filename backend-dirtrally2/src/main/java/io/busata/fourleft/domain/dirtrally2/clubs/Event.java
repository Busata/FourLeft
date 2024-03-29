package io.busata.fourleft.domain.dirtrally2.clubs;


import lombok.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="event")
@Getter
@Setter
@Builder(builderMethodName = "event")
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    private String referenceId;

    private String challengeId;
    private String name;
    private String eventStatus;
    private String vehicleClass;
    private String country;
    private String firstStageCondition;

    private LocalDateTime lastResultCheckedTime;

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    private boolean archived;

    @ManyToOne(fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "championship_id")
    private Championship championship;


    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("referenceId ASC")
    private List<Stage> stages = new ArrayList<>();

    public void updateStages(List<Stage> value) {
        stages.clear();
        stages.addAll(value);
    }

    public boolean isCurrent() {
        return eventStatus.equals("Active");
    }

    public boolean hasEnded() {
        return getEndTime().toLocalDateTime().plusMinutes(5).isBefore(LocalDateTime.now());
    }
    public boolean isPrevious() {
        return eventStatus.equals("Finished");
    }

    public Stage getLastStage() {
        return getStages().get(getStages().size() -1);
    }

    Long getOrder() {
        return Long.valueOf(referenceId);
    }

    public boolean exists() {
        return !this.isArchived();

    }
    public void markAsRemoved() {
        this.archived = true;
    }
}
