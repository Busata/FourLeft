package io.busata.fourleft.api.models.configuration.results;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MergedViewTo extends ResultsViewTo {

    private String name;

    RacenetFilterTo racenetFilter;

    private List<SingleClubViewTo> resultViews;
}
