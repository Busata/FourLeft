package io.busata.fourleft.api.models.configuration.results;


import io.busata.fourleft.domain.configuration.player_restrictions.PlayerFilter;
import io.busata.fourleft.domain.configuration.results_views.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SingleClubViewTo extends ResultsViewTo {
    private long clubId;

    private String name;

    boolean usePowerstage;

    Integer powerStageIndex;

    PlayerFilterTo playerFilter;


    public SingleClubViewTo(UUID id, long clubId, String name, boolean usePowerstage, Integer powerStageIndex, PlayerFilterTo playerFilter) {
        this.setId(id);
        this.clubId = clubId;
        this.name = name;
        this.usePowerstage = usePowerstage;
        this.powerStageIndex = powerStageIndex;
        this.playerFilter = playerFilter;
    }
}
