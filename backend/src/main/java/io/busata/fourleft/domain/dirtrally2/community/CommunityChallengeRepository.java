package io.busata.fourleft.domain.dirtrally2.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityChallengeRepository extends JpaRepository<CommunityChallenge, UUID> {

    List<CommunityChallenge> findBySyncedFalseAndEndedTrue();

    List<CommunityChallenge> findBySyncedTrueAndEndedTrue();

    Optional<CommunityChallenge> findByChallengeId(String id);
}
