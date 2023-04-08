package io.busata.fourleft.domain.configuration.results_views;


import io.busata.fourleft.domain.clubs.models.ClubMember;
import io.busata.fourleft.domain.configuration.ClubView;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

    public abstract Set<Long> getAssociatedClubs();
}
