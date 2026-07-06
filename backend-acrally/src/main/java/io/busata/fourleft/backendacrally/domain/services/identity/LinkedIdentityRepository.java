package io.busata.fourleft.backendacrally.domain.services.identity;

import io.busata.fourleft.backendacrally.domain.models.identity.IdentityProvider;
import io.busata.fourleft.backendacrally.domain.models.identity.LinkedIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkedIdentityRepository extends JpaRepository<LinkedIdentity, UUID> {

    Optional<LinkedIdentity> findByProviderAndProviderUserId(IdentityProvider provider, String providerUserId);

    Optional<LinkedIdentity> findByUserIdAndProvider(UUID userId, IdentityProvider provider);

    List<LinkedIdentity> findByUserId(UUID userId);
}
