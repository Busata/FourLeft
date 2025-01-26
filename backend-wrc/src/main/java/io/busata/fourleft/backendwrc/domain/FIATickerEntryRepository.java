package io.busata.fourleft.backendwrc.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FIATickerEntryRepository extends JpaRepository<FIATickerEntry, UUID> {
    Long countByEventId(String eventId);

    List<FIATickerEntry> findByEventId(String eventId);

    void deleteByReferenceId(UUID referenceId);

}
