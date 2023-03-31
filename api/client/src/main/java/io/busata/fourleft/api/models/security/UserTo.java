package io.busata.fourleft.api.models.security;

import java.util.List;

public record UserTo(
        String name,
        List<String> roles) {
}
