package io.busata.fourleft.domain.aggregators.results;


import io.busata.fourleft.common.MergeMode;
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

    @Enumerated(EnumType.STRING)
    MergeMode mergeMode;

    @OneToMany(cascade = CascadeType.ALL)
    List<SingleClubView> resultViews;

    @ManyToOne(cascade = CascadeType.ALL)
    RacenetFilter racenetFilter;

    @Override
    public Set<Long> getAssociatedClubs() {
        return resultViews.stream().map(ResultsView::getAssociatedClubs).flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
