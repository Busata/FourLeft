package io.busata.fourleft.domain.aggregators.results;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PartitionView extends ResultsView {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="results_view_id")
    ResultsView resultsView;

    @ManyToMany(cascade = CascadeType.ALL)
    List<RacenetFilter> partitionElements;

    @Override
    public Set<Long> getAssociatedClubs() {
        return resultsView.getAssociatedClubs();
    }
}
