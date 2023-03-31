package io.busata.fourleft.api.models.views;

import io.busata.fourleft.domain.configuration.results_views.BadgeType;

public record ViewPropertiesTo(
        boolean powerStage,
        BadgeType badgeType
) {
}
