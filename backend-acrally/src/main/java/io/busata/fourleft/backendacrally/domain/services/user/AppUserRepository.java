package io.busata.fourleft.backendacrally.domain.services.user;

import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    boolean existsByDisplayNameIgnoreCase(String displayName);
}
