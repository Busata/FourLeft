package io.busata.fourleft.endpoints.discord.fieldmapper.service;

import io.busata.fourleft.api.models.FieldMappingTo;
import io.busata.fourleft.domain.discord.models.FieldMapping;
import org.springframework.stereotype.Component;

@Component
public class FieldMappingToFactory {

    public FieldMappingTo create(FieldMapping mapping) {
        return new FieldMappingTo(
                mapping.getId(),
                mapping.getName(),
                mapping.getValue(),
                mapping.getType(),
                mapping.isMappedByUser()
        );
    }
}
