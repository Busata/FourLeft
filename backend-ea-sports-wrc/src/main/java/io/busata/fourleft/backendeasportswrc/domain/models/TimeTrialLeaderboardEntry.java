package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * One fetched row of a {@link TimeTrialCombination}'s Racenet leaderboard. Unlike the append-only
 * {@link TimeTrialProbe} history, this is the <em>current</em> board: a fetch replaces every row for
 * the combination, so the table always holds the latest snapshot. Times are stored as the raw
 * Racenet-formatted strings ({@code "mm:ss.SSS"}); no parsing on the way in.
 */
@Entity
@Table(name = "time_trial_entry")
@Getter
@NoArgsConstructor
public class TimeTrialLeaderboardEntry {

    @Id
    @GeneratedValue
    private UUID id;

    /** {@link TimeTrialCombination#getId()} this entry belongs to. */
    @Column(nullable = false)
    private String combinationId;

    private String ssid;
    private String displayName;
    private String wrcPlayerId;

    private Long rank;
    // Spring's naming strategy maps nationalityID -> "nationalityid" (no split before a trailing
    // all-caps run); pin it to the migration's snake_case column instead.
    @Column(name = "nationality_id")
    private Long nationalityID;
    private Long platform;

    private String vehicle;

    private String time;
    private String differenceToFirst;
    private String timePenalty;

    /** Per-sector split times ({@code "hh:mm:ss.fffffff"}), stored as a jsonb array. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> splits;

    private Instant fetchedAt;

    @Builder
    public TimeTrialLeaderboardEntry(String combinationId, String ssid, String displayName, String wrcPlayerId,
                                     Long rank, Long nationalityID, Long platform, String vehicle,
                                     String time, String differenceToFirst, String timePenalty,
                                     List<String> splits, Instant fetchedAt) {
        this.combinationId = combinationId;
        this.ssid = ssid;
        this.displayName = displayName;
        this.wrcPlayerId = wrcPlayerId;
        this.rank = rank;
        this.nationalityID = nationalityID;
        this.platform = platform;
        this.vehicle = vehicle;
        this.time = time;
        this.differenceToFirst = differenceToFirst;
        this.timePenalty = timePenalty;
        this.splits = splits;
        this.fetchedAt = fetchedAt;
    }

    /** Stable identity of the driver on the board: ssid, else wrcPlayerId, else display name. */
    public String getPlayerKey() {
        return Optional.ofNullable(ssid)
                .or(() -> Optional.ofNullable(wrcPlayerId))
                .orElse(displayName);
    }
}
