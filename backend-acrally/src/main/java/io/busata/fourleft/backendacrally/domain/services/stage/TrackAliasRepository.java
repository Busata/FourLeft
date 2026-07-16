package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.TrackAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackAliasRepository extends JpaRepository<TrackAlias, UUID> {

    List<TrackAlias> findAllByOrderByRawNameAsc();

    /** Resolve a telemetry track string from a session to its alias (exact match). */
    Optional<TrackAlias> findByRawName(String rawName);

    boolean existsByRawName(String rawName);
}
