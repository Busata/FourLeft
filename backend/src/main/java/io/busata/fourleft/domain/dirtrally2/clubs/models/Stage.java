package io.busata.fourleft.domain.dirtrally2.clubs.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Table(name="stage")
@Entity
@Getter
@Setter
public class Stage {

    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="event_id")
    private Event event;
}
