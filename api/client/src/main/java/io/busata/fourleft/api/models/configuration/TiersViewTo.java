package io.busata.fourleft.api.models.configuration;

import io.busata.fourleft.api.models.tiers.TierTo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class TiersViewTo extends ResultsViewTo {
    boolean usePowerStage;
    int defaultPowerstageIndex;
    List<TierTo> tiers;
}
