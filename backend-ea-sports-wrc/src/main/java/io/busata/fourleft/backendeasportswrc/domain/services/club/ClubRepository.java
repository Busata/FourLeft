package io.busata.fourleft.backendeasportswrc.domain.services.club;

import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

interface ClubRepository extends JpaRepository<Club, String> {

    // The stage subquery MUST be correlated to e (e2.id = e.id): without it, ":leaderboardId IN
    // (every stage leaderboard id in the table)" is true for a stage board on EVERY event row,
    // making this UPDATE stamp + lock the whole event table across all clubs (full-table write ->
    // deadlocks under concurrent imports). Correlated, it targets only the owning event.
    @Query("UPDATE Event e SET e.lastLeaderboardUpdate=:timestamp " +
            "WHERE e.leaderboardId = :leaderboardId OR :leaderboardId IN " +
            "(SELECT s.leaderboardId FROM Event e2 JOIN e2.stages s WHERE e2.id = e.id)")
    @Modifying
    void markBoardAsUpdated(@Param("leaderboardId") String leaderboardId, @Param("timestamp") LocalDateTime time);

}