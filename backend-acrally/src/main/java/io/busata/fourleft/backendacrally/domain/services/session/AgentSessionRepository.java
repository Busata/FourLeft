package io.busata.fourleft.backendacrally.domain.services.session;

import io.busata.fourleft.backendacrally.domain.models.session.AgentSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentSessionRepository extends JpaRepository<AgentSession, UUID> {

    List<AgentSession> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
}
