package io.busata.fourleft.domain.infrastructure;

import lombok.Getter;

public enum FourLeftRole {

    ADMIN("ROLE_admin");

    @Getter
    private final String roleName;

    FourLeftRole(String roleName) {
        this.roleName = roleName;
    }
}
