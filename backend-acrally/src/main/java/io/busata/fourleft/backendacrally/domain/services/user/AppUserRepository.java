package io.busata.fourleft.backendacrally.domain.services.user;

import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDisplayNameIgnoreCase(String displayName);
}
