package io.busata.fourleft.api.models.configuration.results;


import io.busata.fourleft.domain.configuration.player_restrictions.RacenetFilterMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RacenetFilterTo {

    UUID id;
    private String name;
    RacenetFilterMode filterMode;
    List<String> racenetNames;
}
