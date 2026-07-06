package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** An agent API key as shown on the account page — never carries the token itself. */
public record ApiKeyTo(UUID id, String label, LocalDateTime createdAt, LocalDateTime lastUsedAt, boolean revoked) {
}
