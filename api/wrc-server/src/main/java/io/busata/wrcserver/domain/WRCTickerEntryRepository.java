package io.busata.wrcserver.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WRCTickerEntryRepository extends JpaRepository<WRCTickerEntry, UUID> {
    List<WRCTickerEntry> findAllByEventId(String eventId);
}