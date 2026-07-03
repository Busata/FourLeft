package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeTrialCombinationRepository extends JpaRepository<TimeTrialCombination, String> {

    /**
     * A page of combinations for the catalog, matched by a case-insensitive LIKE across the id key and
     * the location / route / vehicle-class names. The caller always passes a concrete pattern
     * ({@code %%} matches everything) so Postgres can infer the parameter type. Ordered for a stable,
     * grouped listing (location, then stage, then surface, then class).
     */
    @Query("""
            SELECT t FROM TimeTrialCombination t
            WHERE LOWER(t.id) LIKE LOWER(:searchPattern)
               OR LOWER(t.location) LIKE LOWER(:searchPattern)
               OR LOWER(t.route) LIKE LOWER(:searchPattern)
               OR LOWER(t.vehicleClass) LIKE LOWER(:searchPattern)
            ORDER BY t.locationId, t.routeId, t.surfaceCondition, t.vehicleClassId
            """)
    Page<TimeTrialCombination> search(@Param("searchPattern") String searchPattern, Pageable pageable);
}
