package io.busata.fourleft.domain.configuration.event_restrictions.repository;

import io.busata.fourleft.domain.configuration.event_restrictions.models.ViewEventRestrictions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ViewEventRestrictionsRepository extends JpaRepository<ViewEventRestrictions, UUID> {
    Optional<ViewEventRestrictions> findByResultViewIdAndChallengeIdAndEventId(UUID resultViewId, String challengeId, String eventId);
}