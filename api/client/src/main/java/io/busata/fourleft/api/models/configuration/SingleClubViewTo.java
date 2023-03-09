package io.busata.fourleft.api.models.configuration;


import io.busata.fourleft.domain.configuration.results_views.BadgeType;
import io.busata.fourleft.domain.configuration.player_restrictions.PlayerRestrictionFilterType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SingleClubViewTo extends ResultsViewTo {
    private long clubId;
    BadgeType badgeType;
    PlayerRestrictionsTo playerRestrictions;
    @Override
    public boolean includesClub(long clubId) {
        return this.clubId == clubId;
    }
}
