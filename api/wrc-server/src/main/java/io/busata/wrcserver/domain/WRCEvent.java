package io.busata.wrcserver.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class WRCEvent {

    @Id
    @GeneratedValue
    private UUID id;

    private boolean active;

    private String name;

    private String wrcReferenceId;

    public WRCEvent(String name, String wrcReferenceId, boolean isActive) {
        this.active = isActive;
        this.name = name;
        this.wrcReferenceId = wrcReferenceId;
    }
}
