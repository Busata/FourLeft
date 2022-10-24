package io.busata.fourleft.domain.tiers.repository;

import io.busata.fourleft.domain.tiers.models.TierEventRestrictions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TierEventRestrictionsRepository extends JpaRepository<TierEventRestrictions, UUID> {
    Optional<TierEventRestrictions> findByTierIdAndChallengeIdAndEventId(UUID tierId, String challengeId, String eventId);
}