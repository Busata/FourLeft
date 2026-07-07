package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A user as seen by an administrator on the user-management page. Never carries the password hash.
 */
public record AdminUserTo(UUID id, String email, String displayName, String status, boolean admin,
                          LocalDateTime createdAt) {
}
