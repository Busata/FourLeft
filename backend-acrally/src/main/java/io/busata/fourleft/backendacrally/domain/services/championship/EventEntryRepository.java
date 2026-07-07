package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.EventEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventEntryRepository extends JpaRepository<EventEntry, UUID> {

    Optional<EventEntry> findByEventIdAndVariantIdAndUserId(UUID eventId, UUID variantId, UUID userId);

    /** One stage's board, fastest first. */
    List<EventEntry> findByEventIdAndVariantIdOrderByTotalMsAsc(UUID eventId, UUID variantId);

    /** Every entry for an event (all stages) — the overall standings are derived from these. */
    List<EventEntry> findByEventId(UUID eventId);

    /** A single driver's entries across an event's stages (their personal results). */
    List<EventEntry> findByEventIdAndUserId(UUID eventId, UUID userId);
}
