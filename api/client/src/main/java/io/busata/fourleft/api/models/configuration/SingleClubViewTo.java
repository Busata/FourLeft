package io.busata.fourleft.api.models.configuration;


import io.busata.fourleft.domain.configuration.results_views.BadgeType;
import io.busata.fourleft.domain.configuration.results_views.PlayerRestrictions;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SingleClubViewTo extends ResultsViewTo {
    private long clubId;
    BadgeType badgeType;
    PlayerRestrictions playerRestrictions;
    List<String> players;

}
