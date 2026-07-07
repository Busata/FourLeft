package io.busata.fourleft.backendacrally.domain.services.car;

import io.busata.fourleft.backendacrally.domain.models.car.CarAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarAliasRepository extends JpaRepository<CarAlias, UUID> {

    List<CarAlias> findAllByOrderByRawNameAsc();

    /** Resolve a raw car string from a result/telemetry to its alias (exact match). */
    Optional<CarAlias> findByRawName(String rawName);
}
