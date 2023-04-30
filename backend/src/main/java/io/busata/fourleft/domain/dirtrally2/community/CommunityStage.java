package io.busata.fourleft.domain.dirtrally2.community;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
@Setter
@Getter
public class CommunityStage {
    @Id
    @GeneratedValue
    UUID id;

    String stageId;

    String name;
    String country;
    String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="event_id")
    private CommunityEvent event;

}
