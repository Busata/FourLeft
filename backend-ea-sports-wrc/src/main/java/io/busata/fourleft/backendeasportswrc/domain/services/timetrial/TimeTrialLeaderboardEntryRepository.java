package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialLeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TimeTrialLeaderboardEntryRepository extends JpaRepository<TimeTrialLeaderboardEntry, UUID> {

    List<TimeTrialLeaderboardEntry> findByCombinationId(String combinationId);

    /**
     * Just (player key, time) for a board's current rows — the churn comparison needs nothing else,
     * and loading full entities for a 50k board would defeat the streaming fetch's memory ceiling.
     * The key mirrors {@code TimeTrialLeaderboardEntry.getPlayerKey()}: ssid, else wrcPlayerId, else
     * display name.
     */
    @Query("""
            select coalesce(e.ssid, e.wrcPlayerId, e.displayName), e.time
            from TimeTrialLeaderboardEntry e
            where e.combinationId = :combinationId
            """)
    List<Object[]> findPlayerTimes(@Param("combinationId") String combinationId);

    /**
     * Drop the superseded generation once a fresh fetch has fully landed: delete this board's rows from
     * any earlier fetch (strictly older {@code fetchedAt}), leaving only the just-completed snapshot.
     * This is the "swap" — the old rows stay live for reads and churn until the new ones are all in, so
     * a crash mid-fetch leaves the previous snapshot intact rather than a blank/partial board. Also
     * clears any partial rows orphaned by an earlier crashed run (they too predate this run).
     */
    @Transactional
    @Modifying
    @Query("delete from TimeTrialLeaderboardEntry e where e.combinationId = :combinationId and e.fetchedAt < :fetchedAt")
    void deleteSupersededBy(@Param("combinationId") String combinationId, @Param("fetchedAt") Instant fetchedAt);

    /** Remove every row for a board — used when Racenet reports the board no longer exists. */
    @Transactional
    @Modifying
    @Query("delete from TimeTrialLeaderboardEntry e where e.combinationId = :combinationId")
    void deleteByCombinationId(@Param("combinationId") String combinationId);
}
