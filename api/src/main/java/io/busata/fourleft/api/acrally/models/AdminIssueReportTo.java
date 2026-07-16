package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A user-submitted issue report as listed on the admin page. Carries attachment names + sizes only;
 * the bytes are fetched via the per-report download endpoints.
 */
public record AdminIssueReportTo(UUID id, UUID userId, String userDisplayName, String description,
                                 String agentVersion, String saveGameName, int saveGameSize,
                                 String agentLogName, int agentLogSize, LocalDateTime createdAt) {
}
