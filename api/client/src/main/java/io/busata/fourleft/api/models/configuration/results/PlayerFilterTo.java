package io.busata.fourleft.api.models.configuration.results;

import io.busata.fourleft.domain.configuration.player_restrictions.RacenetFilterMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PlayerFilterTo {

    private RacenetFilterMode playerFilterType;
    private List<String> racenetNames;
}
