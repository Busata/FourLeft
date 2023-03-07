package io.busata.fourleft.domain.configuration.results_views;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MergedView extends ResultsView implements MultipleClubsView {
    private String name;

    boolean powerStage;
    int defaultPowerstageIndex; // -1 for last.


    @Enumerated(EnumType.STRING)
    BadgeType badgeType;
    @OneToMany(cascade = CascadeType.ALL)
    List<SingleClubView> resultViews;
    @Override
    public List<Long> getAssociatedClubs() {
        return resultViews.stream().map(SingleClubView::getClubId).toList();
    }
}
