package io.busata.fourleft.domain.configuration.results_views;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TieredView extends ResultsView {

    boolean usePowerStage;
    int defaultPowerstageIndex; // -1 for last.

    String name;

    @Enumerated(EnumType.STRING)
    BadgeType badgeType;

    @OneToMany(cascade = CascadeType.ALL)
    List<SingleClubView> resultViews;

    @Override
    public List<Long> getAssociatedClubs() {
        return resultViews.stream().map(SingleClubView::getClubId).collect(Collectors.toList());
    }
}
