package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialProbe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TimeTrialProbeRepository extends JpaRepository<TimeTrialProbe, Long> {

    /**
     * The most recent probe for each of the given combinations (skips combinations never probed).
     * {@code DISTINCT ON} + {@code ORDER BY combination_id, probed_at DESC} keeps one row per
     * combination, the newest — the current state used by the catalog view.
     */
    @Query(value = """
            SELECT DISTINCT ON (combination_id) *
            FROM time_trial_probe
            WHERE combination_id IN (:combinationIds)
            ORDER BY combination_id, probed_at DESC
            """, nativeQuery = true)
    List<TimeTrialProbe> findLatestForCombinations(@Param("combinationIds") Collection<String> combinationIds);
}
