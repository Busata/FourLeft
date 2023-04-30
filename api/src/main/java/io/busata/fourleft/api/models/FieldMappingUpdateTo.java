package io.busata.fourleft.api.models;


import io.busata.fourleft.common.FieldMappingType;

public record FieldMappingUpdateTo(String name, String value, FieldMappingType fieldMappingType) {
}
