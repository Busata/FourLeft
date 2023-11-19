package io.busata.fourleft.backendeasportswrc.application.fieldmapping;

import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMapping;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingContext;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingRepository;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EAWRCFieldMapper {

    private final FieldMappingRepository fieldMappingRepository;

    public String getDiscordField(String name, FieldMappingType type) {
        return this.getDiscordField(name, type, "");
    }
    public String getDiscordField(String name, FieldMappingType type, String note) {
        return this.fieldMappingRepository.findFieldMapping(type, name, FieldMappingContext.DISCORD).map(FieldMapping::getValue).orElseGet(() -> {
            return saveMissingMapping(name, type, FieldMappingContext.DISCORD, note);
        });
    }

    private String saveMissingMapping(String name, FieldMappingType type, FieldMappingContext context, String note) {
        var fieldMapping = new FieldMapping(name, type, context);
        fieldMapping.setNote(note);
        fieldMapping = this.fieldMappingRepository.save(fieldMapping);

        return fieldMapping.getValue();
    }


}
