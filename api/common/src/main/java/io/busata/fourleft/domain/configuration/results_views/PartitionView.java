package io.busata.fourleft.domain.configuration.results_views;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

@Entity
@Getter
public class PartitionView extends ResultsView {

    @ManyToOne
    @JoinColumn(name="results_view_id")
    ResultsView resultsView;

    @OneToMany(mappedBy = "partitionView")
    List<PartitionElement> partitionElements;

    @Override
    public Set<Long> getAssociatedClubs() {
        return resultsView.getAssociatedClubs();
    }
}
