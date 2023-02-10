package io.busata.fourleft.domain.clubs.repository;

import io.busata.fourleft.domain.clubs.models.BoardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BoardEntryRepository extends JpaRepository<BoardEntry, UUID> {

    @Query("select distinct name from BoardEntry")
    List<String> findDistinctNames();

    List<BoardEntry> findByName(String name);

    @Query("select distinct be.name from BoardEntry be where be.name not in (select pi.racenet from PlayerInfo pi)")
    List<String> findNamesWithoutPlayerInfo();

    @Query("select distinct be.name from BoardEntry be where be.name in (select pi.racenet from PlayerInfo pi where pi.syncedPlatform=false) and be.leaderboard.id=:leaderboardId ")
    List<String> findUnsyncedNamesForLeaderboard(UUID leaderboardId);

    //@Query("select be.name from BoardEntry be where be.name not in (select pi.racenet from PlayerInfo pi where pi.syncedPlatform=false) group by be.name having count(be.id) > :participations ")
    //@Query("select be.leaderboard.id from BoardEntry be where be.name in (select be2.name from BoardEntry be2 where be2.name not in (select pi.racenet from PlayerInfo pi where pi.syncedPlatform=true) group by be2.name having count(be2.id) > :participations) group by be.leaderboard.id order by count(be.leaderboard.id) DESC")
    @Query(nativeQuery = true, value="select cast(be.leaderboard_id as varchar) from board_entry be join (select be.leaderboard_id as leaderboard_id, count(be.id) as leaderboard_amount from board_entry be group by be.leaderboard_id having count(be.id) > 500 order by count(be.id) DESC) as leaderboard_counts on be.leaderboard_id = leaderboard_counts.leaderboard_id where be.name in (select be.name from board_entry be where be.name not in (select pi.racenet from player_info pi where pi.synced_platform=true) group by be.name having count(be.id) > :participations) order by leaderboard_counts.leaderboard_amount desc;")
    List<UUID> findUnsyncedLeaderboardsByParticipations(@Param("participations") long participations);
}
