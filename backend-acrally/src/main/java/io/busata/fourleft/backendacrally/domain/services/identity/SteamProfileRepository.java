package io.busata.fourleft.backendacrally.domain.services.identity;

import io.busata.fourleft.backendacrally.domain.models.identity.SteamProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SteamProfileRepository extends JpaRepository<SteamProfile, String> {
}
