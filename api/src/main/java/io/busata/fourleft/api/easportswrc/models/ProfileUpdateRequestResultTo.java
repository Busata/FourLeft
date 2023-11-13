package io.busata.fourleft.api.easportswrc.models;

import java.util.UUID;

public record ProfileUpdateRequestResultTo(UUID requestId, boolean foundProfile) {
}
