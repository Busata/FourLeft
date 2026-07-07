package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.EventVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventVariantRepository extends JpaRepository<EventVariant, UUID> {

    List<EventVariant> findAllByEventIdOrderByPositionAsc(UUID eventId);

    List<EventVariant> findAllByEventIdIn(List<UUID> eventIds);

    void deleteByEventId(UUID eventId);
}
