package io.busata.fourleft.backendeasportswrc.domain.services.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ImportTarget;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImportTargetRepository extends JpaRepository<ImportTarget, Long> {

    Optional<ImportTarget> findByTypeAndRef(ImportType type, String ref);

    List<ImportTarget> findByType(ImportType type);

    /**
     * Claim targets that are due to run. Same SKIP LOCKED trick as the job queue:
     * each scheduler tick (across instances) takes a disjoint slice of due targets.
     */
    @Query(value = """
            SELECT * FROM import_target
            WHERE enabled = true AND next_run_at <= now()
            ORDER BY next_run_at
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<ImportTarget> claimDue(@Param("limit") int limit);

    /** Push the next run out by the target's current cadence (DB-authoritative time). */
    @Modifying
    @Query(value = """
            UPDATE import_target
            SET next_run_at = now() + make_interval(secs => interval_sec)
            WHERE id = :id
            """, nativeQuery = true)
    void reschedule(@Param("id") Long id);
}
