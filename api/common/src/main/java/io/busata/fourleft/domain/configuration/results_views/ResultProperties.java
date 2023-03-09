package io.busata.fourleft.domain.configuration.results_views;

import io.busata.fourleft.domain.configuration.player_restrictions.PlayerRestrictionFilterType;

import java.util.List;

public interface ResultProperties {

    boolean isPowerStage();
    int getPowerStageIndex();

    BadgeType getBadgeType();

}
