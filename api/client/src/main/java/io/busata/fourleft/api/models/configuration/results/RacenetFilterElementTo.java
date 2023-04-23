package io.busata.fourleft.api.models.configuration.results;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RacenetFilterElementTo {

    private String name;
    List<String> racenetNames;
}
