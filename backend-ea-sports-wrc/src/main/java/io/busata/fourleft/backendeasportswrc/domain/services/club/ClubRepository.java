package io.busata.fourleft.backendeasportswrc.domain.services.club;

import io.busata.fourleft.api.easportswrc.models.ClubReferenceTo;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

interface ClubRepository extends JpaRepository<Club, String> {

    /** All clubs as lightweight {id, name} references (no championship graph loaded), name-sorted. */
    @Query("select new io.busata.fourleft.api.easportswrc.models.ClubReferenceTo(c.id, c.clubName) from Club c order by lower(c.clubName)")
    List<ClubReferenceTo> findAllReferences();

    /** Every club id — the work-list for exporting all clubs. */
    @Query("select c.id from Club c")
    List<String> findAllClubIds();

    // Stamp the event that owns this leaderboard, either directly (event.leaderboard_id) or via one
    // of its stages. Resolve the (usually single) target event id up front, then update it by PK.
    //
    // The two ways an event can own the board are UNIONed rather than OR'd: an `OR` in the WHERE
    // (or an OR nested inside a correlated sub-select, the previous shape) forces Postgres to walk
    // EVERY event row and re-evaluate the stage sub-select per row -> a ~10.8k x 54k scan, ~20-30s
    // per call. Fired once per board per import and run 5-wide, that scan saturated the DB and
    // wedged the whole import queue. As a UNION, each branch is driven by its own index
    // (idx_event_leaderboard_id / idx_stage_leaderboard_id, added in V014) and returns a tiny id
    // set, so the outer UPDATE only touches the matching rows. See V014 migration.
    @Query(value = """
            UPDATE event SET last_leaderboard_update = :timestamp
            WHERE id IN (
                SELECT id FROM event WHERE leaderboard_id = :leaderboardId
                UNION
                SELECT es.event_id FROM event_stages es
                    JOIN stage s ON s.id = es.stages_id
                    WHERE s.leaderboard_id = :leaderboardId
            )
            """, nativeQuery = true)
    @Modifying
    void markBoardAsUpdated(@Param("leaderboardId") String leaderboardId, @Param("timestamp") LocalDateTime time);

}