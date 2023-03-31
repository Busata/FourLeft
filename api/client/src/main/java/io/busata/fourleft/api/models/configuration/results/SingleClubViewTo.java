package io.busata.fourleft.api.models.configuration.results;


import io.busata.fourleft.domain.configuration.player_restrictions.PlayerFilter;
import io.busata.fourleft.domain.configuration.results_views.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SingleClubViewTo extends ResultsViewTo {
    private long clubId;

    private String name;

    boolean usePowerstage;

    Integer powerStageIndex;

    PlayerFilterTo playerFilter;


}
