package io.busata.fourleft.api.models.configuration.results;

import io.busata.fourleft.common.BadgeType;
import lombok.Getter;

@Getter
public class CommunityChallengeViewTo extends ResultsViewTo {

    private boolean postDailies;
    private boolean postWeeklies;
    private boolean postMonthlies;
    private BadgeType badgeType;

    public CommunityChallengeViewTo(boolean postDailies, boolean postWeeklies, boolean postMonthlies, BadgeType badgeType) {
        this.postDailies = postDailies;
        this.postWeeklies = postWeeklies;
        this.postMonthlies = postMonthlies;
        this.badgeType = badgeType;
    }
}
