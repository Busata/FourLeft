package io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FieldMapping {

    @Id
    @GeneratedValue
    private UUID id;


    private String name;
    private String value;

    @Enumerated(EnumType.STRING)
    private FieldMappingType type;

    @Enumerated(EnumType.STRING)
    private FieldMappingContext context;

    @Setter
    private String note;

    private boolean mappedByUser;

    public FieldMapping(String name, FieldMappingType type, FieldMappingContext context) {
        this.name = name;
        this.value = type.getDefaultValue(context);
        this.type = type;
        this.context = context;
        this.mappedByUser = false;
    }


}
