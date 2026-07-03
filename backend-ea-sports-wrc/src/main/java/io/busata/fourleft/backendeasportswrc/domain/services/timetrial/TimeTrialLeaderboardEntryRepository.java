package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialLeaderboardEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** The combinations that currently have stored entries — the boards worth showing in the browser. */
    @Query("select distinct e.combinationId from TimeTrialLeaderboardEntry e")
    List<String> findDistinctCombinationIds();

    /**
     * Every board a player appears on, by display name — the reverse lookup behind the profile page.
     * Only the latest generation of each board is considered (so an in-progress re-fetch doesn't
     * surface a stale duplicate). A player has at most one row per board, so this stays small.
     */
    @Query("""
            select e from TimeTrialLeaderboardEntry e
            where e.displayName = :name
              and e.fetchedAt = (select max(e2.fetchedAt) from TimeTrialLeaderboardEntry e2 where e2.combinationId = e.combinationId)
            """)
    List<TimeTrialLeaderboardEntry> findLatestByDisplayName(@Param("name") String name);

    /**
     * Distinct display names containing {@code q} (case-insensitive) — the driver autocomplete. Names
     * that start with the query rank first, then alphabetically. Backed by the trigram index on
     * lower(display_name). {@link Pageable} caps the number of suggestions returned.
     */
    @Query("""
            select e.displayName from TimeTrialLeaderboardEntry e
            where lower(e.displayName) like lower(concat('%', :q, '%'))
            group by e.displayName
            order by min(case when lower(e.displayName) like lower(concat(:q, '%')) then 0 else 1 end), e.displayName
            """)
    List<String> suggestDisplayNames(@Param("q") String q, Pageable pageable);

    /**
     * One page of a board's live snapshot. Filters to the latest {@code fetchedAt} so an in-progress
     * re-fetch (which transiently holds two generations) still reads a single clean board; after a
     * fetch completes only one generation remains anyway. Order/paging come from the {@link Pageable}.
     */
    @Query(value = """
            select e from TimeTrialLeaderboardEntry e
            where e.combinationId = :combinationId
              and e.fetchedAt = (select max(e2.fetchedAt) from TimeTrialLeaderboardEntry e2 where e2.combinationId = :combinationId)
            """,
            countQuery = """
            select count(e) from TimeTrialLeaderboardEntry e
            where e.combinationId = :combinationId
              and e.fetchedAt = (select max(e2.fetchedAt) from TimeTrialLeaderboardEntry e2 where e2.combinationId = :combinationId)
            """)
    Page<TimeTrialLeaderboardEntry> findLatestPage(@Param("combinationId") String combinationId, Pageable pageable);

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
