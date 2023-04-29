package io.busata.fourleft.api.models.views;


import io.busata.fourleft.common.BadgeType;

public record ViewPropertiesTo(
        boolean powerStage,
        BadgeType badgeType
) {
}
