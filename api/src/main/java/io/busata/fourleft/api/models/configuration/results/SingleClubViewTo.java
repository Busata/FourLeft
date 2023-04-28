package io.busata.fourleft.api.models.configuration.results;


import lombok.Getter;

import java.util.UUID;

@Getter
public class SingleClubViewTo extends ResultsViewTo {
    private long clubId;

    private String name;

    boolean usePowerstage;

    Integer powerStageIndex;

    RacenetFilterTo racenetFilter;


    public SingleClubViewTo(UUID id, long clubId, String name, boolean usePowerstage, Integer powerStageIndex, RacenetFilterTo racenetFilter) {
        this.setId(id);
        this.clubId = clubId;
        this.name = name;
        this.usePowerstage = usePowerstage;
        this.powerStageIndex = powerStageIndex;
        this.racenetFilter = racenetFilter;
    }
}
