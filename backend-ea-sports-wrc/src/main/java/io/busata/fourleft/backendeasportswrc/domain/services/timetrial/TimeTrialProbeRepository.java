package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialProbe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TimeTrialProbeRepository extends JpaRepository<TimeTrialProbe, Long> {

    /**
     * The most recent probe for every combination that has one. {@code DISTINCT ON} +
     * {@code ORDER BY combination_id, probed_at DESC} keeps one row per combination — the newest —
     * which is the current state shown in the catalog view.
     */
    @Query(value = """
            SELECT DISTINCT ON (combination_id) *
            FROM time_trial_probe
            ORDER BY combination_id, probed_at DESC
            """, nativeQuery = true)
    List<TimeTrialProbe> findAllLatest();

    /** The most recent probe for a single combination — its current entry count / validity. */
    @Query(value = """
            SELECT * FROM time_trial_probe
            WHERE combination_id = :combinationId
            ORDER BY probed_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<TimeTrialProbe> findLatestByCombinationId(String combinationId);
}
