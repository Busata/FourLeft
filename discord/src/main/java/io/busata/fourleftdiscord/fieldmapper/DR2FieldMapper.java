package io.busata.fourleftdiscord.fieldmapper;

import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.FieldMappingTo;
import io.busata.fourleft.api.models.FieldMappingRequestTo;
import io.busata.fourleft.common.FieldMappingContext;
import io.busata.fourleft.common.FieldMappingType;
import io.busata.fourleftdiscord.client.FourLeftClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DR2FieldMapper {
    private final FourLeftClient fourLeftClient;

    public Color createColour(String value) {
        String fieldMapping = getFieldMapping(value, FieldMappingType.COLOUR);

        Integer[] array = Arrays.stream(fieldMapping.split(",")).map(Integer::parseInt).toArray(Integer[]::new);

        return Color.of(array[0], array[1], array[2]);
    }

    public String createEmoticon(String value) {
        return getFieldMapping(value, FieldMappingType.EMOTE);
    }

    public String createHumanReadable(String value) {
        return getFieldMapping(value, FieldMappingType.HUMAN_READABLE);
    }

    public String createImage(String value) {
        return getFieldMapping(value, FieldMappingType.IMAGE);
    }

    private String getFieldMapping(String name, FieldMappingType type) {
        return getFieldMappings().stream()
                .filter(fieldMappingTo -> fieldMappingTo.name().equals(name) && fieldMappingTo.fieldMappingType() == type)
                .findFirst()
                .map(FieldMappingTo::value)
                .orElseGet(() -> this.saveMissingMapping(name, type).value());
    }

    private FieldMappingTo saveMissingMapping(String name, FieldMappingType type) {
        return this.fourLeftClient.createFieldMapping(new FieldMappingRequestTo(name, type, FieldMappingContext.BACKEND));
    }

    @Cacheable("field_mapppings")
    public List<FieldMappingTo> getFieldMappings() {
        return this.fourLeftClient.getFieldMappings();
    }

}
