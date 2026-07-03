package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One observation of a {@link TimeTrialCombination}'s Racenet board — did it exist, how many entries,
 * and (once the fetch worker runs) how many entries changed since the previous fetch. Append-only:
 * each observation inserts a new row; the latest row per combination is the current state, and the
 * series over time feeds popularity / smart scheduling (total = size, changed = churn).
 */
@Entity
@Getter
@NoArgsConstructor
public class TimeTrialProbe {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "time_trial_probe_generator")
    @SequenceGenerator(name = "time_trial_probe_generator", sequenceName = "time_trial_probe_seq", allocationSize = 50)
    private Long id;

    /** {@link TimeTrialCombination#getId()} this probe was for. */
    private String combinationId;

    /** Whether the board existed on Racenet at probe time. */
    private boolean boardExists;

    /** Entry count on the board; {@code null} when the board did not exist. */
    private Integer totalEntries;

    /** Entries changed since the previous fetch; {@code null} for probes (which don't fetch entries). */
    private Integer changedEntries;

    private Instant probedAt;

    /** Probe observation: existence + count only; {@code changedEntries} stays null (not fetched). */
    public TimeTrialProbe(String combinationId, boolean boardExists, Integer totalEntries) {
        this(combinationId, boardExists, totalEntries, null);
    }

    /**
     * Fetch observation: existence, count, and how many entries changed since the previous fetch
     * ({@code changedEntries} is the churn signal — new drivers plus improved times).
     */
    public TimeTrialProbe(String combinationId, boolean boardExists, Integer totalEntries, Integer changedEntries) {
        this.combinationId = combinationId;
        this.boardExists = boardExists;
        this.totalEntries = totalEntries;
        this.changedEntries = changedEntries;
        this.probedAt = Instant.now();
    }
}
