package io.busata.fourleft.backendacrally.domain.models.championship;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** A car permitted in an event. The set is an explicit snapshot of car ids. */
@Entity
@Table(name = "event_car")
@Getter
@NoArgsConstructor
public class EventCar {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "car_id", nullable = false)
    private UUID carId;

    public EventCar(UUID eventId, UUID carId) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.carId = carId;
    }
}
