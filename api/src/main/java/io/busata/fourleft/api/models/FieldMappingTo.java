package io.busata.fourleft.api.models;


import java.util.UUID;

public record FieldMappingTo(
        UUID id,
        String name,
        String value,
        FieldMappingType fieldMappingType,
        boolean mappedByUser
) {
}
