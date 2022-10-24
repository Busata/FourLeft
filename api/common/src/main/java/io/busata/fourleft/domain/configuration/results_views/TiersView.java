package io.busata.fourleft.domain.configuration.results_views;


import io.busata.fourleft.domain.tiers.models.Tier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TiersView extends ResultsView {

    boolean usePowerStage;
    int defaultPowerstageIndex; // -1 for last.

    @Enumerated(EnumType.STRING)
    BadgeType badgeType;

    @OneToMany(mappedBy = "tiersView", cascade = CascadeType.ALL)
    List<Tier> tiers;

}
