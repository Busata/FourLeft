package io.busata.fourleft.backendacrally.domain.services.agent;

import io.busata.fourleft.backendacrally.domain.models.agent.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByTokenHash(String tokenHash);

    List<ApiKey> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
