package io.busata.fourleft.api.models.views;


import io.busata.fourleft.api.models.BadgeType;

public record ViewPropertiesTo(
        boolean powerStage,
        BadgeType badgeType
) {
}
