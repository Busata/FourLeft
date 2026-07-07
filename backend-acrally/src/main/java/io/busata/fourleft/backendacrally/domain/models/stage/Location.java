package io.busata.fourleft.backendacrally.domain.models.stage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** A rally location (Greece, Monte Carlo, …) with its nation. Authored by hand; the top of the hierarchy. */
@Entity
@Table(name = "location")
@Getter
@NoArgsConstructor
public class Location {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nation")
    private String nation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Location(String name, String nation) {
        this.id = UUID.randomUUID();
        this.name = name.strip();
        this.nation = normalise(nation);
        this.createdAt = LocalDateTime.now();
    }

    public void update(String name, String nation) {
        this.name = name.strip();
        this.nation = normalise(nation);
        this.updatedAt = LocalDateTime.now();
    }

    private static String normalise(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }
}
