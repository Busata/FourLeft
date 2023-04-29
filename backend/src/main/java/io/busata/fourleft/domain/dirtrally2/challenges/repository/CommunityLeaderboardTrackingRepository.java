package io.busata.fourleft.domain.dirtrally2.challenges.repository;

import io.busata.fourleft.domain.dirtrally2.challenges.models.CommunityLeaderboardTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommunityLeaderboardTrackingRepository extends JpaRepository<CommunityLeaderboardTracking, UUID> {
}
