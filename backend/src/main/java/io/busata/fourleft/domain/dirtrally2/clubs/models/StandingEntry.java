package io.busata.fourleft.domain.dirtrally2.clubs.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "standing_entry")
@Getter
@Setter
public class StandingEntry {
    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "championship_id")
    private Championship championship;

    Long rank;
    String nationality;
    String displayName;
    Long totalPoints;

}