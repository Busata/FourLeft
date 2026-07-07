package io.busata.fourleft.backendacrally.domain.models.car;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The raw car string the game reports, mapped to a catalogue {@link Car}. Collected from results
 * (like a {@code Variant} is for stages); {@code carId} is {@code null} until an admin assigns it.
 * Matching resolves an incoming {@code stage_result.car} up to its car via this table.
 */
@Entity
@Table(name = "car_alias")
@Getter
@NoArgsConstructor
public class CarAlias {

    @Id
    private UUID id;

    @Column(name = "raw_name", nullable = false, updatable = false)
    private String rawName;

    /** The catalogue car this alias names, or {@code null} while unassigned. */
    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CarAlias(String rawName) {
        this.id = UUID.randomUUID();
        this.rawName = rawName;
        this.createdAt = LocalDateTime.now();
    }

    /** Point this alias at a catalogue car ({@code null} to clear the assignment). */
    public void assign(UUID carId) {
        this.carId = carId;
        this.updatedAt = LocalDateTime.now();
    }
}
