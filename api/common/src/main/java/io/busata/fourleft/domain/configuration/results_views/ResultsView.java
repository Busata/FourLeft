package io.busata.fourleft.domain.configuration.results_views;


import io.busata.fourleft.domain.configuration.event_restrictions.models.ViewEventRestrictions;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ResultsView {

    @Id
    @GeneratedValue
    @Setter
    @Getter
    UUID id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resultsView")
    List<ViewEventRestrictions> viewEventRestrictions;

    public abstract Set<Long> getAssociatedClubs();
}
