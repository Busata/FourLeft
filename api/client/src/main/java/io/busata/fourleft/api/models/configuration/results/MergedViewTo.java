package io.busata.fourleft.api.models.configuration.results;

import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MergedViewTo extends ResultsViewTo {

    private String name;

    PlayerFilterTo playerFilter;

    private List<SingleClubViewTo> resultViews;
}
