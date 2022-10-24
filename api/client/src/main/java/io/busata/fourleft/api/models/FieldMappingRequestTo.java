package io.busata.fourleft.api.models;

import io.busata.fourleft.domain.discord.models.FieldMappingType;

public record FieldMappingRequestTo(
        String name,
        FieldMappingType type
) {
}
