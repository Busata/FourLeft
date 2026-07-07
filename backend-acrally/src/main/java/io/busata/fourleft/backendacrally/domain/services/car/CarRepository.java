package io.busata.fourleft.backendacrally.domain.services.car;

import io.busata.fourleft.backendacrally.domain.models.car.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    List<Car> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    /** Resolve a raw car string from a result to the catalogue car it names (case-insensitive). */
    Optional<Car> findFirstByNameIgnoreCase(String name);
}
