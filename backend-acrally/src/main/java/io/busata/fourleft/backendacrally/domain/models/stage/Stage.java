package io.busata.fourleft.backendacrally.domain.models.stage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** A stage within a location. Authored by hand; optionally assigned to a location. */
@Entity
@Table(name = "stage")
@Getter
@NoArgsConstructor
public class Stage {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    /** The owning location, or {@code null} while unassigned. */
    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Stage(String name, UUID locationId) {
        this.id = UUID.randomUUID();
        this.name = name.strip();
        this.locationId = locationId;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String name, UUID locationId) {
        this.name = name.strip();
        this.locationId = locationId;
        this.updatedAt = LocalDateTime.now();
    }
}
