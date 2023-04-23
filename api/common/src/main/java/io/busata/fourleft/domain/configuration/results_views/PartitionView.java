package io.busata.fourleft.domain.configuration.results_views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "partition_view_id")
    List<RacenetFilterElement> partitionElements;

    @Override
    public Set<Long> getAssociatedClubs() {
        return resultsView.getAssociatedClubs();
    }
}
