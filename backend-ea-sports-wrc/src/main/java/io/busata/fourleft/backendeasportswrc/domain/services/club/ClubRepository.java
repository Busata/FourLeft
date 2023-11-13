package io.busata.fourleft.backendeasportswrc.domain.services.club;

import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

interface ClubRepository extends JpaRepository<Club, String> {

    @Query("UPDATE Event e SET e.lastLeaderboardUpdate=:timestamp " +
            "WHERE e.leaderboardId = :leaderboardId OR :leaderboardId IN " +
            "(SELECT s.leaderboardId FROM Event e2 JOIN e2.stages s)")
    @Modifying
    void markBoardAsUpdated(@Param("leaderboardId") String leaderboardId, @Param("timestamp") LocalDateTime time);

}