package io.busata.fourleft.api.models;

import io.busata.fourleft.domain.discord.models.FieldMappingType;

public record FieldMappingUpdateTo(String name, String value, FieldMappingType fieldMappingType) {
}
