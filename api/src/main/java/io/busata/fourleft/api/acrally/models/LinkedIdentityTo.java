package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;

public record LinkedIdentityTo(String provider, String providerUserId, LocalDateTime linkedAt) {
}
