package io.busata.fourleft.api.models;


import io.busata.fourleft.common.FieldMappingContext;
import io.busata.fourleft.common.FieldMappingType;

import java.util.UUID;

public record FieldMappingTo(
        UUID id,
        String name,
        String value,
        FieldMappingType fieldMappingType,
        FieldMappingContext context,
        boolean mappedByUser
) {
}
