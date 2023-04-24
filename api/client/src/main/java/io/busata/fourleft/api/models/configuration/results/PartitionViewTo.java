package io.busata.fourleft.api.models.configuration.results;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PartitionViewTo extends ResultsViewTo {

    private ResultsViewTo resultsView;

    private List<RacenetFilterTo> partitionElements;

}
