package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.EventCar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventCarRepository extends JpaRepository<EventCar, UUID> {

    List<EventCar> findAllByEventId(UUID eventId);

    List<EventCar> findAllByEventIdIn(List<UUID> eventIds);

    void deleteByEventId(UUID eventId);
}
