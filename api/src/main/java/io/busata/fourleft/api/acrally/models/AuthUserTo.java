package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/**
 * The authenticated user as seen by the browser (never carries the password hash).
 */
public record AuthUserTo(UUID id, String email, String displayName, String status) {
}
