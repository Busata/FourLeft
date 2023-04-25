package io.busata.fourleft.api.models.configuration.results;

import io.busata.fourleft.domain.configuration.results_views.MergeMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MergedViewTo extends ResultsViewTo {

    MergeMode mergeMode;

    private String name;

    RacenetFilterTo racenetFilter;

    private List<SingleClubViewTo> resultViews;
}
