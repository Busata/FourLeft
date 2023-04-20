package io.busata.fourleft.api.models.configuration.results;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PartitionElementTo {

    private String name;
    private int order;
    List<String> racenetNames;
}
