package io.busata.fourleft.api.models.configuration;

import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;
import io.busata.fourleft.api.models.tiers.TierTo;
import io.busata.fourleft.api.models.views.SingleResultListTo;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
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
