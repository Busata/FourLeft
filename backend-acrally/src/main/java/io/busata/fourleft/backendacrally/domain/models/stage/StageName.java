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
 * Maps a raw stage identifier (as it arrives on {@code stage_result.stage}) to a
 * human-readable display name. Collected from the results; the display name is assigned
 * afterwards by an admin and used to render stages across the app.
 */
@Entity
@Table(name = "stage_name")
@Getter
@NoArgsConstructor
public class StageName {

    @Id
    private UUID id;

    @Column(name = "raw_name", nullable = false, updatable = false)
    private String rawName;

    /** The readable name; {@code null} until an admin assigns one (results fall back to the raw name). */
    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StageName(String rawName) {
        this.id = UUID.randomUUID();
        this.rawName = rawName;
        this.createdAt = LocalDateTime.now();
    }

    /** Sets (or clears) the readable name. A blank value is normalised to {@code null}. */
    public void rename(String displayName) {
        this.displayName = (displayName == null || displayName.isBlank()) ? null : displayName.strip();
        this.updatedAt = LocalDateTime.now();
    }
}
