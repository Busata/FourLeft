package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialProbe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
}
