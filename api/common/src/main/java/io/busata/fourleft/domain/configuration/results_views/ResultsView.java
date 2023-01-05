package io.busata.fourleft.domain.configuration.results_views;


import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ResultsView {

    @Id
    @GeneratedValue
    @Setter
    UUID id;

    public abstract List<Long> getAssociatedClubs();
}
