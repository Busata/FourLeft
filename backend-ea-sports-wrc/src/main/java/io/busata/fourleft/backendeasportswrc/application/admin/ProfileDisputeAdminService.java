package io.busata.fourleft.backendeasportswrc.application.admin;

import io.busata.fourleft.api.easportswrc.models.ProfileDisputeAdminTo;
import io.busata.fourleft.backendeasportswrc.domain.models.admin.AdminLink;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import io.busata.fourleft.backendeasportswrc.domain.services.profile.ProfileService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordUserDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/** The operator page for profile disputes, behind an expiring admin link. */
@Service
@RequiredArgsConstructor
public class ProfileDisputeAdminService {
    static final Duration LINK_VALIDITY = Duration.ofDays(7);

    private final AdminLinkRepository linkRepository;
    private final ProfileService profileService;
    private final DiscordUserDirectory discordUsers;

    @Transactional
    public UUID newLink(String createdBy) {
        return linkRepository.save(new AdminLink(createdBy, LINK_VALIDITY)).getId();
    }

    public List<ProfileDisputeAdminTo> openDisputes(UUID linkId) {
        requireValid(linkId);
        return profileService.openDisputes().stream().map(this::toTo).toList();
    }

    public List<ProfileDisputeAdminTo> dismiss(UUID linkId, String ssid) {
        requireValid(linkId);
        profileService.dismissDispute(ssid);
        return openDisputes(linkId);
    }

    public List<ProfileDisputeAdminTo> transfer(UUID linkId, String ssid) {
        requireValid(linkId);
        profileService.transferToDisputer(ssid);
        return openDisputes(linkId);
    }

    private void requireValid(UUID linkId) {
        if (!linkRepository.findById(linkId).map(AdminLink::isValid).orElse(false)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private ProfileDisputeAdminTo toTo(Profile profile) {
        return new ProfileDisputeAdminTo(
                profile.getId(),
                profile.getRacenet(),
                profile.getDisplayName(),
                discordUsers.lookup(profile.getDiscordId()),
                discordUsers.lookup(profile.getDisputedByDiscordId()),
                profile.getDisputedAt());
    }
}
