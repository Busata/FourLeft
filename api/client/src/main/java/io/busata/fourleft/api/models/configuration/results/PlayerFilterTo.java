package io.busata.fourleft.api.models.configuration.results;

import io.busata.fourleft.domain.configuration.player_restrictions.PlayerFilterType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Getter
public class PlayerFilterTo {

    private PlayerFilterType playerFilterType;
    private List<String> racenetNames;
}
