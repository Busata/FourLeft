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
 * The unique key that arrives on {@code stage_result.stage} — the leaf of the hierarchy.
 * Collected from results; the display name and stage assignment are set afterwards by an admin.
 * Results resolve their raw key up the chain (variant → stage → location) to render a readable name.
 */
@Entity
@Table(name = "variant")
@Getter
@NoArgsConstructor
public class Variant {

    @Id
    private UUID id;

    @Column(name = "raw_name", nullable = false, updatable = false)
    private String rawName;

    /** The readable name for this variant; {@code null} until an admin assigns one. */
    @Column(name = "display_name")
    private String displayName;

    /** The stage this variant belongs to, or {@code null} while unassigned. */
    @Column(name = "stage_id")
    private UUID stageId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Variant(String rawName) {
        this.id = UUID.randomUUID();
        this.rawName = rawName;
        this.createdAt = LocalDateTime.now();
    }

    /** Sets the readable name (blank → {@code null}) and the stage assignment. */
    public void update(String displayName, UUID stageId) {
        this.displayName = (displayName == null || displayName.isBlank()) ? null : displayName.strip();
        this.stageId = stageId;
        this.updatedAt = LocalDateTime.now();
    }
}
