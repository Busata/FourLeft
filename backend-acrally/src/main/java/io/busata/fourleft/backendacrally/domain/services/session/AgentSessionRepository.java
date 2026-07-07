package io.busata.fourleft.backendacrally.domain.services.session;

import io.busata.fourleft.backendacrally.domain.models.session.AgentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AgentSessionRepository extends JpaRepository<AgentSession, UUID> {

    List<AgentSession> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);

    /** OPEN sessions whose last sign of life (heartbeat, else creation) predates the cutoff. */
    @Query("""
            select s from AgentSession s
            where s.status = io.busata.fourleft.backendacrally.domain.models.session.SessionStatus.OPEN
              and coalesce(s.lastHeartbeatAt, s.createdAt) < :cutoff
            """)
    List<AgentSession> findOpenAndSilentSince(@Param("cutoff") LocalDateTime cutoff);
}
