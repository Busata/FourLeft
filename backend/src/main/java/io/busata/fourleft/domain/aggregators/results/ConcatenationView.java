package io.busata.fourleft.domain.aggregators.results;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ConcatenationView extends ResultsView {

    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    List<SingleClubView> resultViews;

    @Override
    public Set<Long> getAssociatedClubs() {
        return resultViews.stream().map(ResultsView::getAssociatedClubs).flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
