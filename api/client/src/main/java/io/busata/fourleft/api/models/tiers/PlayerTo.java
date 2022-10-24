package io.busata.fourleft.api.models.tiers;

import java.util.List;
import java.util.UUID;

public record PlayerTo(UUID id, String racenet, List<TierTo> tiers) {
}
