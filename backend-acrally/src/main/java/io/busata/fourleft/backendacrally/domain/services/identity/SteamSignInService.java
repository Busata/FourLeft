package io.busata.fourleft.backendacrally.domain.services.identity;

import io.busata.fourleft.backendacrally.domain.models.identity.IdentityProvider;
import io.busata.fourleft.backendacrally.domain.models.identity.LinkedIdentity;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Steam-only sign-in: a verified steamID64 either resolves to its existing account or
 * provisions one on the spot. The Steam identity IS the credential — there is no separate
 * registration step, so "one Steam = one account" holds by construction (the unique
 * {@code (provider, provider_user_id)} constraint), which is what ban enforcement hangs on.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SteamSignInService {

    private final LinkedIdentityRepository identities;
    private final AppUserRepository users;
    private final SteamProfileService steamProfileService;
    private final SteamProfileRepository steamProfiles;

    /**
     * Resolve the account for a verified steamID64, creating it on first sign-in. The
     * initial display name is the Steam persona (when the profile is fetchable), made
     * unique with a numeric suffix if taken; the user can change it afterwards.
     */
    @Transactional
    public AppUser signIn(String steamId64) {
        var existing = identities.findByProviderAndProviderUserId(IdentityProvider.STEAM, steamId64);
        if (existing.isPresent()) {
            // Keep the persona/avatar/ban snapshot fresh on each sign-in (best-effort).
            steamProfileService.refreshAsync(steamId64);
            return users.findById(existing.get().getUserId()).orElseThrow();
        }

        // First sign-in: fetch the profile synchronously so the account starts with a
        // recognizable name, then provision user + identity.
        steamProfileService.refresh(steamId64);
        String displayName = uniqueDisplayName(seedDisplayName(steamId64));
        try {
            AppUser user = users.save(new AppUser(displayName));
            identities.save(new LinkedIdentity(IdentityProvider.STEAM, steamId64, user.getId()));
            log.info("Provisioned account {} ({}) for steam id {}", user.getId(), displayName, steamId64);
            return user;
        } catch (DataIntegrityViolationException concurrentSignIn) {
            // Lost a race with a concurrent first sign-in of the same Steam — use theirs.
            LinkedIdentity identity = identities
                    .findByProviderAndProviderUserId(IdentityProvider.STEAM, steamId64)
                    .orElseThrow(() -> concurrentSignIn);
            return users.findById(identity.getUserId()).orElseThrow();
        }
    }

    /** Steam persona when available, otherwise a neutral driver handle. */
    private String seedDisplayName(String steamId64) {
        return steamProfiles.findById(steamId64)
                .map(p -> p.getPersonaName())
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .orElse("Driver " + steamId64.substring(steamId64.length() - 5));
    }

    /** Display names are unique case-insensitively; suffix collisions like "Name (2)". */
    private String uniqueDisplayName(String seed) {
        String base = seed.length() > 50 ? seed.substring(0, 50).trim() : seed;
        String candidate = base;
        for (int i = 2; users.existsByDisplayNameIgnoreCase(candidate); i++) {
            candidate = "%s (%d)".formatted(base, i);
        }
        return candidate;
    }
}
