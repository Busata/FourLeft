package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;

/** What the browser approval page shows for a pending user code. */
public record PairLookupResultTo(String userCode, String label, LocalDateTime expiresAt) {
}
