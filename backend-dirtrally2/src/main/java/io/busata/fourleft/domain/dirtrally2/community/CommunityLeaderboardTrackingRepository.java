package io.busata.fourleft.domain.dirtrally2.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommunityLeaderboardTrackingRepository extends JpaRepository<CommunityLeaderboardTracking, UUID> {
}
