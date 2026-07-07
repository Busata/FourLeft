package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.ApiKeyTo;
import io.busata.fourleft.api.acrally.models.AuthUserTo;
import io.busata.fourleft.api.acrally.models.LinkedIdentityTo;
import io.busata.fourleft.api.acrally.models.SteamProfileTo;
import io.busata.fourleft.api.acrally.models.UpdateDisplayNameRequestTo;
import io.busata.fourleft.backendacrally.domain.models.identity.IdentityProvider;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.agent.ApiKeyService;
import io.busata.fourleft.backendacrally.domain.services.identity.LinkedIdentityRepository;
import io.busata.fourleft.backendacrally.domain.services.identity.SteamProfileRepository;
import io.busata.fourleft.backendacrally.domain.services.user.UserProfileService;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/acrally-api/account")
@RequiredArgsConstructor
public class AccountEndpoint {

    private final LinkedIdentityRepository linkedIdentityRepository;
    private final SteamProfileRepository steamProfileRepository;
    private final ApiKeyService apiKeyService;
    private final UserProfileService userProfileService;

    @GetMapping("/identities")
    public List<LinkedIdentityTo> myIdentities(@AuthenticationPrincipal AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return linkedIdentityRepository.findByUserId(principal.getId()).stream()
                .map(identity -> new LinkedIdentityTo(
                        identity.getProvider().name(),
                        identity.getProviderUserId(),
                        identity.getLinkedAt()))
                .toList();
    }

    /** The Steam profile snapshot for the signed-in user, or 204 if none is linked/fetched yet. */
    @GetMapping("/steam")
    public ResponseEntity<SteamProfileTo> mySteamProfile(@AuthenticationPrincipal AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return linkedIdentityRepository.findByUserIdAndProvider(principal.getId(), IdentityProvider.STEAM)
                .flatMap(identity -> steamProfileRepository.findById(identity.getProviderUserId()))
                .map(profile -> new SteamProfileTo(
                        profile.getPersonaName(),
                        profile.getAvatarUrl(),
                        profile.getProfileUrl(),
                        profile.getAccountCreated(),
                        profile.getVisibilityState(),
                        profile.isVacBanned(),
                        profile.getGameBanCount(),
                        profile.isCommunityBanned()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/keys")
    public List<ApiKeyTo> myKeys(@AuthenticationPrincipal AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return apiKeyService.list(principal.getId()).stream()
                .map(key -> new ApiKeyTo(
                        key.getId(),
                        key.getLabel(),
                        key.getCreatedAt(),
                        key.getLastUsedAt(),
                        !key.isActive()))
                .toList();
    }

    @PostMapping("/keys/{id}/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeKey(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        apiKeyService.revoke(id, principal.getId());
    }

    /** Rename the account. The display name is cosmetic; identity stays the Steam anchor. */
    @PostMapping("/display-name")
    public AuthUserTo changeDisplayName(@RequestBody UpdateDisplayNameRequestTo request,
                                        @AuthenticationPrincipal AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        AppUser user = userProfileService.changeDisplayName(principal.getId(), request.displayName());
        return new AuthUserTo(user.getId(), user.getDisplayName(), user.getStatus().name(), user.isAdmin());
    }
}
