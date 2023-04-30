package io.busata.fourleft.domain.discord.bot.models;

import io.busata.fourleft.common.FieldMappingType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table
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

    private boolean mappedByUser;

    public FieldMapping(String name, FieldMappingType type) {
        this.name = name;
        this.value = type.getDefaultValue();
        this.type = type;
        this.mappedByUser = false;
    }


}
