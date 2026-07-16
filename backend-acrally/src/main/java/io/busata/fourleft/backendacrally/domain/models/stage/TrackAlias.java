package io.busata.fourleft.backendacrally.domain.models.stage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The track string live telemetry reports, mapped to a {@link Variant}. Telemetry names are
 * display names, per-variant and localized ("Alsace Montée" vs "Alsace Descente"; a French client
 * sends "Pays de Galles Hafren Forest"), a separate namespace from {@code variant.raw_name} (the
 * save-file key). Collected from sessions (like a {@link io.busata.fourleft.backendacrally.domain.models.car.CarAlias}
 * is from results); {@code variantId} is {@code null} until an admin assigns it. The mapping decides
 * whether a freshly opened session is a run of the armed stage and must bind — an unassigned or
 * unknown name errs toward binding, so gaps can never re-open discard-grinding.
 */
@Entity
@Table(name = "track_alias")
@Getter
@NoArgsConstructor
public class TrackAlias {

    @Id
    private UUID id;

    @Column(name = "raw_name", nullable = false, updatable = false)
    private String rawName;

    /** The variant this alias names, or {@code null} while unassigned. */
    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TrackAlias(String rawName) {
        this.id = UUID.randomUUID();
        this.rawName = rawName;
        this.createdAt = LocalDateTime.now();
    }

    /** Point this alias at a variant ({@code null} to clear the assignment). */
    public void assign(UUID variantId) {
        this.variantId = variantId;
        this.updatedAt = LocalDateTime.now();
    }
}
