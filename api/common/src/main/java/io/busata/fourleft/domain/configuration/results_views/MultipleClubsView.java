package io.busata.fourleft.domain.configuration.results_views;

import java.util.List;

public interface MultipleClubsView {

    String getName();
    boolean isPowerStage();
    BadgeType getBadgeType();

    List<SingleClubView> getResultViews();
}
