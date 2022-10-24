package io.busata.fourleft.domain.configuration.results_views;

import java.util.List;

public interface ResultProperties {

    boolean isPowerStage();
    int getPowerStageIndex();
    List<String> getPlayerNames();
    PlayerRestrictions getPlayerRestrictions();

    BadgeType getBadgeType();

}
