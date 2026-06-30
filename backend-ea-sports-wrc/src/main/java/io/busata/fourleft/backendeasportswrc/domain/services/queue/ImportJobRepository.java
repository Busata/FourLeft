package io.busata.fourleft.backendeasportswrc.domain.services.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ImportJob;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportJobStatus;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    /**
     * Recent jobs for the dashboard, newest first, with optional status and
     * club-id (ref) search filters. Pass {@code null} for a filter to ignore it.
     */
    @Query("""
            SELECT j FROM ImportJob j
            WHERE j.type = :type
              AND (:status IS NULL OR j.status = :status)
              AND (:search IS NULL OR LOWER(j.ref) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY j.id DESC
            """)
    List<ImportJob> search(@Param("type") ImportType type,
                           @Param("status") ImportJobStatus status,
                           @Param("search") String search,
                           Pageable pageable);

    long countByTypeAndStatus(ImportType type, ImportJobStatus status);

    /** Prune terminal jobs older than a cutoff (keeps the table bounded). */
    @Modifying
    @Query(value = """
            DELETE FROM import_job
            WHERE status = :status AND created_at < now() - make_interval(hours => :hours)
            """, nativeQuery = true)
    int deleteByStatusOlderThanHours(@Param("status") String status, @Param("hours") int hours);

    /**
     * Claim the next runnable job. {@code FOR UPDATE SKIP LOCKED} means concurrent
     * workers each grab a different row instead of blocking on the same one. The
     * returned entity is managed, so the caller flipping it to RUNNING is flushed
     * on transaction commit (which also releases the row lock).
     */
    @Query(value = """
            SELECT * FROM import_job
            WHERE status = 'PENDING' AND run_after <= now()
            ORDER BY run_after
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """, nativeQuery = true)
    Optional<ImportJob> claimNext();

    /** True if this recurring target already has a job in flight (prevents pile-up). */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM import_job
                WHERE target_id = :targetId AND status IN ('PENDING', 'RUNNING')
            )
            """, nativeQuery = true)
    boolean hasActiveJobForTarget(@Param("targetId") Long targetId);

    /** Recover jobs whose worker died mid-flight: RUNNING for too long -> back to PENDING. */
    @Modifying
    @Query(value = """
            UPDATE import_job
            SET status = 'PENDING', locked_at = NULL
            WHERE status = 'RUNNING' AND locked_at < now() - make_interval(secs => :staleSeconds)
            """, nativeQuery = true)
    int requeueStale(@Param("staleSeconds") int staleSeconds);
}
