package io.busata.fourleft.backendacrally.infrastructure.security;

import java.util.UUID;

/** Principal for agent (API-key) authenticated requests to the ingestion endpoints. */
public record AgentPrincipal(UUID userId, UUID apiKeyId) {
}
