package io.busata.fourleft.application.discord;

import io.busata.fourleft.api.models.FieldMappingTo;
import io.busata.fourleft.domain.discord.bot.models.FieldMapping;
import io.busata.fourleft.infrastructure.common.Factory;
import org.springframework.stereotype.Component;

@Factory
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
