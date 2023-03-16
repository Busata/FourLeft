package io.busata.fourleft.domain.configuration.results_views;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MergeResultsView extends ResultsView {
    private String name;
    @OneToMany(cascade = CascadeType.ALL)
    List<SingleClubView> resultViews;
    @Override
    public Set<Long> getAssociatedClubs() {
        return resultViews.stream().map(ResultsView::getAssociatedClubs).flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
