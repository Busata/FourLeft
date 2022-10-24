package io.busata.fourleft.api.models;

import io.busata.fourleft.domain.discord.models.FieldMappingType;

import java.util.UUID;

public record FieldMappingTo(
        UUID id,
        String name,
        String value,
        FieldMappingType fieldMappingType,
        boolean mappedByUser
) {
}
