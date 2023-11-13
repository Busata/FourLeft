package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.ProfileTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestTo;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import io.busata.fourleft.backendeasportswrc.domain.services.profile.ProfileService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProfileEndpoint {
    private final ProfileService service;
    private final DiscordGateway discordGateway;

    @PostMapping("/api_v2/profile/request")
    public ProfileUpdateRequestResultTo requestTrackingUpdate(@RequestBody ProfileUpdateRequestTo request) {
        discordGateway.createMessage(1173372471207018576L, new SimpleDiscordMessageTo("**%s** (%s) requested profile update for racenet: **%s**".formatted(request.userName(), request.discordId(), request.racenet()), List.of()));

        return service.requestUpdate(request.discordId(), request.racenet()).map(uuid -> {
            return new ProfileUpdateRequestResultTo(uuid, true);
        }).orElse(new ProfileUpdateRequestResultTo(null, false));

    }

    @GetMapping("/api_v2/profile/{requestId}")
    public Optional<ProfileTo> getProfile(@PathVariable UUID requestId) {
        return service.getProfile(requestId).map(profile -> new ProfileTo(
                profile.getId(),
                profile.getDisplayName(),
                profile.getController(),
                profile.getPlatform(),
                profile.getPeripheral(),
                profile.getRacenet(),
                profile.isTrackDiscord()
        ));
    }

    @PostMapping("/api_v2/profile/{requestId}")
    public ProfileTo updateProfile(@PathVariable UUID requestId, @RequestBody ProfileTo profile) {
        Profile updatedProfile = service.updateProfile(requestId, profile);

        discordGateway.createMessage(1173372471207018576L, new SimpleDiscordMessageTo("**__Profile updated__**\n**Display name: ** %s\n**Controller: ** %s\n**Peripheral: ** %s\n**Platform: **%s\n**Tracking discord: ** %s".formatted(profile.displayName(), profile.controller(), profile.peripheral(), profile.platform(), profile.trackDiscord()), List.of()));

        return new ProfileTo(
                updatedProfile.getId(),
                updatedProfile.getDisplayName(),
                updatedProfile.getController(),
                updatedProfile.getPlatform(),
                updatedProfile.getPeripheral(),
                updatedProfile.getRacenet(),
                updatedProfile.isTrackDiscord()
        );
    }
}
