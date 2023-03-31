package io.busata.wrcserver.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WRCEventRepository extends JpaRepository<WRCEvent, UUID> {
    Optional<WRCEvent> findByActiveIsTrue();
}