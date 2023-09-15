package io.busata.fourleft.api.models;

import java.util.UUID;

public record AliasRequestResultTo(boolean aliasExists, UUID requestId) {
}
