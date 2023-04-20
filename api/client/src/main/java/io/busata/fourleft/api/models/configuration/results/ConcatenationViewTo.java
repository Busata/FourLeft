package io.busata.fourleft.api.models.configuration.results;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ConcatenationViewTo extends ResultsViewTo {

    private String name;

    private List<SingleClubViewTo> resultViews;
}
