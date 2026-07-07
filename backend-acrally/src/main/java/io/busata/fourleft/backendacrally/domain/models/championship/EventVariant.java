package io.busata.fourleft.backendacrally.domain.models.championship;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** One drivable variant in an event, at a fixed position in the stage running order. */
@Entity
@Table(name = "event_variant")
@Getter
@NoArgsConstructor
public class EventVariant {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Column(name = "position", nullable = false)
    private int position;

    public EventVariant(UUID eventId, UUID variantId, int position) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.variantId = variantId;
        this.position = position;
    }
}
