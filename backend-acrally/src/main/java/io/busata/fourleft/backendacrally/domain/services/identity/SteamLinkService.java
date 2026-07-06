package io.busata.fourleft.backendacrally.domain.services.identity;

import io.busata.fourleft.backendacrally.domain.models.identity.IdentityProvider;
import io.busata.fourleft.backendacrally.domain.models.identity.LinkedIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SteamLinkService {

    private final LinkedIdentityRepository repository;

    /**
     * Attaches a verified Steam id to the given account.
     *
     * <ul>
     *   <li>Re-linking the same Steam to the same account is a no-op (idempotent).</li>
     *   <li>Linking a Steam already owned by another account is refused — this is what
     *       stops a banned user re-registering and re-attaching the same Steam.</li>
     *   <li>An account that already has a Steam can't silently swap it for another, which
     *       would otherwise defeat the anchor.</li>
     * </ul>
     */
    @Transactional
    public LinkedIdentity linkSteam(UUID userId, String steamId64) {
        var existingForSteam = repository.findByProviderAndProviderUserId(IdentityProvider.STEAM, steamId64);
        if (existingForSteam.isPresent()) {
            if (existingForSteam.get().getUserId().equals(userId)) {
                return existingForSteam.get();
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This Steam account is already linked to another account.");
        }

        if (repository.findByUserIdAndProvider(userId, IdentityProvider.STEAM).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Your account already has a Steam account linked.");
        }

        try {
            return repository.save(new LinkedIdentity(IdentityProvider.STEAM, steamId64, userId));
        } catch (DataIntegrityViolationException concurrentLink) {
            // Lost a race against a concurrent link of the same Steam / same account.
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This Steam account is already linked.");
        }
    }
}
