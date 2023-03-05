package io.busata.fourleft.api.models.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class TiersViewTo extends ResultsViewTo {
    boolean usePowerStage;
    int defaultPowerstageIndex;
    List<SingleClubViewTo> tiers;

    @Override
    public boolean includesClub(long clubId) {
        return this.tiers.stream().map(SingleClubViewTo::getClubId).anyMatch(id -> clubId == id);
    }
}
