package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
