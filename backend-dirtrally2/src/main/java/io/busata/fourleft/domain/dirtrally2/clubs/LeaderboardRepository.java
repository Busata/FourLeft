package io.busata.fourleft.domain.dirtrally2.clubs;

import io.busata.fourleft.common.LeaderboardKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, UUID> {

    Optional<Leaderboard> findLeaderboardByChallengeIdAndEventIdAndStageId(String challengeId, String eventId, String stageId);

    default Optional<Leaderboard> findLeaderboard(LeaderboardKey leaderboardKey) {
        return findLeaderboardByChallengeIdAndEventIdAndStageId(leaderboardKey.challengeId(), leaderboardKey.eventId(), String.valueOf(leaderboardKey.stageId()));
    }

    @Query("select l from Leaderboard l join BoardEntry be on l.id = be.leaderboard.id where be.name=:racenet and l.challengeId in (select e.challengeId from Event e where e.eventStatus='Finished' and e.endTime < '2023-07-10T00:00:00Z') group by l.id")
    List<Leaderboard> findLeaderboardsWhereRacenetPresent(@Param("racenet") String racenet);

    @Query(value = """
                     select be.rank, be.is_dnf as isDnf, be.name, cc.end_time as challengeDate, lo.count as total, cc.type from board_entry be
                              join leaderboard l on be.leaderboard_id = l.id
                              right outer join community_challenge cc on l.challenge_id = cc.challenge_id
                            join (select count(*) as count, l.id as id from board_entry be join leaderboard l on be.leaderboard_id = l.id group by l.id) as LO on LO.id = l.id
                             where lower(be.name)=lower(:name)
                     order by cc.end_time desc;
            """, nativeQuery = true)
    List<CommunityChallengeSummaryProjection> findCommunityChallengeSummary(@Param("name") String name);
}
