package io.busata.fourleft.api.models;


import io.busata.fourleft.common.FieldMappingContext;
import io.busata.fourleft.common.FieldMappingType;

public record FieldMappingRequestTo(
        String name,
        FieldMappingType type,

        FieldMappingContext context
) {
}
