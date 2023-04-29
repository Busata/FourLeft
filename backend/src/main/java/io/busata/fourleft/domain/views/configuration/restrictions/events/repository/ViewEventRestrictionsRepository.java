package io.busata.fourleft.domain.views.configuration.restrictions.events.repository;

import io.busata.fourleft.domain.views.configuration.restrictions.events.models.ViewEventRestrictions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ViewEventRestrictionsRepository extends JpaRepository<ViewEventRestrictions, UUID> {
    Optional<ViewEventRestrictions> findByResultsViewIdAndChallengeIdAndEventId(UUID resultsViewId, String challengeId, String eventId);
}