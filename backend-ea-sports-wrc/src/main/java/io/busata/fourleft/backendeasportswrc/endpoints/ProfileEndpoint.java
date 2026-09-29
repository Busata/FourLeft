package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.events.ProfileUpdatedEvent;
import io.busata.fourleft.api.easportswrc.models.ProfileClaimRequestTo;
import io.busata.fourleft.api.easportswrc.models.ProfilePageTo;
import io.busata.fourleft.api.easportswrc.models.ProfileTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo.Outcome;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestTo;
import io.busata.fourleft.api.easportswrc.models.TierPlayerTo;
import io.busata.fourleft.backendeasportswrc.application.admin.ProfileDisputeAdminService;
import io.busata.fourleft.backendeasportswrc.application.players.PlayerSuggestions;
import io.busata.fourleft.backendeasportswrc.domain.services.profile.ProfileService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProfileEndpoint {
    private static final long PROFILE_LOG_CHANNEL = 1173372471207018576L;
    private static final String DISPUTES_PAGE = "https://fourleft.io/easportswrc/admin/%s/disputes";

    private final ProfileService service;
    private final PlayerSuggestions playerSuggestions;
    private final ProfileDisputeAdminService disputeAdmin;

    private final ApplicationEventPublisher eventPublisher;
    private final DiscordGateway discordGateway;

    @PostMapping("/api_v2/profile/request")
    public ProfileUpdateRequestResultTo requestTrackingUpdate(@RequestBody ProfileUpdateRequestTo request) {
        String racenet = request.racenet() == null || request.racenet().isBlank() ? "(none, own profile)" : request.racenet();
        ProfileUpdateRequestResultTo result = service.requestUpdate(request.discordId(), request.racenet());

        log("**%s** (%s) requested profile update for racenet: **%s** → %s".formatted(request.userName(), request.discordId(), racenet, result.outcome()));
        return result;
    }

    @GetMapping("/api_v2/profile/{requestId}")
    public ResponseEntity<ProfilePageTo> getProfile(@PathVariable UUID requestId) {
        return ResponseEntity.of(service.getPage(requestId));
    }

    @GetMapping("/api_v2/profile/{requestId}/suggest")
    public ResponseEntity<List<TierPlayerTo>> suggestPlayers(@PathVariable UUID requestId, @RequestParam String q) {
        if (!service.requestExists(requestId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(playerSuggestions.suggest(q));
    }

    @PostMapping("/api_v2/profile/{requestId}/claim")
    public ProfileUpdateRequestResultTo claim(@PathVariable UUID requestId, @RequestBody ProfileClaimRequestTo request) {
        ProfileUpdateRequestResultTo result = service.claimFromPage(requestId, request.playerId());

        log("Profile page claim of racenet **%s** (%s, request %s) → %s".formatted(result.racenet(), request.playerId(), requestId, result.outcome()));
        if (result.outcome() == Outcome.LINKED) {
            eventPublisher.publishEvent(new ProfileUpdatedEvent());
        }
        return result;
    }

    /** Confirms disputing a racenet account someone else holds; only offered on the page, after a claim said so. */
    @PostMapping("/api_v2/profile/{requestId}/dispute")
    public ProfileUpdateRequestResultTo dispute(@PathVariable UUID requestId, @RequestBody ProfileClaimRequestTo request) {
        ProfileUpdateRequestResultTo result = service.disputeFromPage(requestId, request.playerId());

        log("Profile page dispute of racenet **%s** (%s, request %s) → %s".formatted(result.racenet(), request.playerId(), requestId, result.outcome()));
        if (result.outcome() == Outcome.DISPUTED) {
            log("Review open disputes [here](%s) (link valid for 7 days).".formatted(DISPUTES_PAGE.formatted(disputeAdmin.newLink("dispute-log"))));
        }
        if (result.outcome() == Outcome.LINKED) {
            eventPublisher.publishEvent(new ProfileUpdatedEvent());
        }
        return result;
    }

    @PostMapping("/api_v2/profile/{requestId}/withdraw")
    public ProfilePageTo withdrawDispute(@PathVariable UUID requestId) {
        ProfilePageTo page = service.withdrawDispute(requestId);

        log("Dispute withdrawn (request %s).".formatted(requestId));
        return page;
    }

    @PostMapping("/api_v2/profile/{requestId}")
    public ProfileTo updateProfile(@PathVariable UUID requestId, @RequestBody ProfileTo profile) {
        ProfileTo updatedProfile = service.updateProfile(requestId, profile);

        log("**__Profile updated__**\n**Display name: ** %s\n**Controller: ** %s\n**Peripheral: ** %s\n**Platform: **%s\n**Tracking discord: ** %s".formatted(profile.displayName(), profile.controller(), profile.peripheral(), profile.platform(), profile.trackDiscord()));

        eventPublisher.publishEvent(new ProfileUpdatedEvent());

        return updatedProfile;
    }

    private void log(String message) {
        discordGateway.createMessage(PROFILE_LOG_CHANNEL, new SimpleDiscordMessageTo(message, List.of()));
    }
}
