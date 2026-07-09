package io.busata.fourleft.backendeasportswrc.domain.services.leaderboards;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboard;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ClubLeaderboardRepository extends JpaRepository<ClubLeaderboard, String> {

    @Query("select cl.entries from ClubLeaderboard cl where cl.id=:id")
    List<ClubLeaderboardEntry> findEntries(@Param("id")String leaderboardId);

    @Query("select cl from ClubLeaderboard cl left join fetch cl.entries where cl.id in :ids")
    List<ClubLeaderboard> findAllWithEntriesByIds(@Param("ids") List<String> leaderboardIds);

    @Query("select distinct entry.vehicle from ClubLeaderboard cl join cl.entries entry " +
            "where cl.id in :ids and entry.vehicle is not null order by entry.vehicle")
    List<String> findDistinctVehicles(@Param("ids") List<String> leaderboardIds);
}