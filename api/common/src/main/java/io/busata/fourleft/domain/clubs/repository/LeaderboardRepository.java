package io.busata.fourleft.domain.clubs.repository;

import io.busata.fourleft.domain.clubs.models.Leaderboard;
import io.busata.fourleft.domain.clubs.models.LeaderboardKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, UUID> {

    Optional<Leaderboard> findLeaderboardByChallengeIdAndEventIdAndStageId(String challengeId, String eventId, String stageId);

    default Optional<Leaderboard> findLeaderboard(LeaderboardKey leaderboardKey) {
        return findLeaderboardByChallengeIdAndEventIdAndStageId(leaderboardKey.challengeId(), leaderboardKey.eventId(),String.valueOf(leaderboardKey.stageId()));
    }
}
