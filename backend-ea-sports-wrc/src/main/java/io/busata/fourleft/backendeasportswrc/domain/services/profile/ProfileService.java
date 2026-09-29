package io.busata.fourleft.backendeasportswrc.domain.services.profile;

import io.busata.fourleft.api.easportswrc.models.ProfileClaimState;
import io.busata.fourleft.api.easportswrc.models.ProfileDisputeTo;
import io.busata.fourleft.api.easportswrc.models.ProfilePageTo;
import io.busata.fourleft.api.easportswrc.models.ProfileTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo.Outcome;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.ProfileUpdateRequest;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.projections.RacenetInfo;
import io.busata.fourleft.common.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Discord ↔ racenet links. A racenet account (profile, keyed by ssid) is held by at most one discord user:
 * the first to claim it. Someone else claiming it has to confirm on the profile page, and only then marks it
 * DISPUTED — the holder keeps it until resolved. VERIFIED accounts can't be disputed, an account has at most
 * one disputer, and a discord user has at most one open dispute, so nobody can dispute their way through random
 * profiles. A discord user holds one profile; claiming another releases the previous one.
 * Every edit goes through a private link (profile update request) that only works while its discord user
 * still holds the profile.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileUpdateRequestRepository updateRequestRepository;
    private final ProfileRepository profileRepository;
    private final ClubLeaderboardService leaderboardService;

    /** /wrc profile: without a racenet name, opens the user's own profile (or the page to pick one). */
    @Transactional
    public ProfileUpdateRequestResultTo requestUpdate(String discordId, String racenet) {
        if (racenet == null || racenet.isBlank()) {
            return heldProfile(discordId)
                    .map(profile -> result(newRequest(discordId, profile.getId()), Outcome.LINKED, profile))
                    .orElseGet(() -> pick(discordId));
        }
        return leaderboardService.findRacenet(racenet.trim())
                .map(info -> {
                    Profile profile = profileFor(info);
                    Outcome outcome = claim(discordId, profile);
                    return result(requestFor(discordId, outcome, profile), outcome, profile);
                })
                .orElseGet(() -> pick(discordId));
    }

    /** The page picked a racenet account; on success the link now edits that profile. */
    @Transactional
    public ProfileUpdateRequestResultTo claimFromPage(UUID requestId, String ssid) {
        ProfileUpdateRequest request = updateRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        RacenetInfo info = leaderboardService.findRacenetBySsid(ssid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Profile profile = profileFor(info);
        Outcome outcome = claim(request.getDiscordId(), profile);
        if (outcome == Outcome.LINKED) {
            request.bindTo(profile.getId());
        }
        return result(requestId, outcome, profile);
    }

    /** The page confirmed disputing the racenet account it tried to claim. */
    @Transactional
    public ProfileUpdateRequestResultTo disputeFromPage(UUID requestId, String ssid) {
        ProfileUpdateRequest request = updateRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Profile profile = profileRepository.findById(ssid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Outcome outcome = dispute(request.getDiscordId(), profile);
        if (outcome == Outcome.LINKED) {
            request.bindTo(profile.getId());
        }
        return result(requestId, outcome, profile);
    }

    public boolean requestExists(UUID requestId) {
        return updateRequestRepository.existsById(requestId);
    }

    /** Empty for an unknown link; a page without profile when the user still has to pick their racenet name. */
    @Transactional(readOnly = true)
    public Optional<ProfilePageTo> getPage(UUID requestId) {
        return updateRequestRepository.findById(requestId).map(this::page);
    }

    /** The link's discord user withdraws their dispute (whichever account it's on), freeing them to dispute again. */
    @Transactional
    public ProfilePageTo withdrawDispute(UUID requestId) {
        ProfileUpdateRequest request = updateRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        profileRepository.findByDisputedByDiscordId(request.getDiscordId()).ifPresent(Profile::clearDispute);
        return page(request);
    }

    public List<Profile> openDisputes() {
        return profileRepository.findByClaimStateOrderByDisputedAtAsc(ProfileClaimState.DISPUTED);
    }

    /** Operator decision: the holder keeps the account. */
    @Transactional
    public void dismissDispute(String ssid) {
        openDispute(ssid).clearDispute();
    }

    /** Operator decision: the disputer takes over the account (and lets go of the one they held, if any). */
    @Transactional
    public void transferToDisputer(String ssid) {
        Profile profile = openDispute(ssid);
        profileRepository.findByDiscordId(profile.getDisputedByDiscordId()).forEach(Profile::release);
        profile.transferToDisputer();
    }

    @Transactional
    public ProfileTo updateProfile(UUID requestId, ProfileTo updatedData) {
        Profile profile = updateRequestRepository.findById(requestId)
                .flatMap(this::editableProfile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        profile.setPeripheral(updatedData.peripheral());
        profile.setPlatform(updatedData.platform());
        profile.setController(updatedData.controller());
        profile.setTrackDiscord(updatedData.trackDiscord());
        profile.setDisplayName(updatedData.displayName());

        return toTo(profileRepository.save(profile));
    }

    public Optional<Profile> getProfileById(String id) {
        return profileRepository.findById(id);
    }

    private Profile profileFor(RacenetInfo info) {
        return profileRepository.findById(info.ssid())
                .orElseGet(() -> new Profile(info.ssid(), info.racenet(), null, Platform.fromEASportsWRCId(info.platform()), true));
    }

    /** Links a free account; for someone else's, says whether the user may dispute it (CONFIRM_DISPUTE) or why not. */
    private Outcome claim(String discordId, Profile profile) {
        if (profile.isHeldBy(discordId)) {
            return Outcome.LINKED;
        }
        if (profile.getDiscordId() == null) {
            profileRepository.findByDiscordId(discordId).forEach(Profile::release);
            profile.claimBy(discordId);
            profileRepository.save(profile);
            return Outcome.LINKED;
        }
        if (profile.getClaimState() == ProfileClaimState.VERIFIED) {
            return Outcome.VERIFIED_BY_OTHER;
        }
        if (profile.getClaimState() == ProfileClaimState.DISPUTED) {
            return discordId.equals(profile.getDisputedByDiscordId()) ? Outcome.DISPUTED : Outcome.ALREADY_DISPUTED;
        }
        if (profileRepository.existsByDisputedByDiscordId(discordId)) {
            return Outcome.DISPUTE_LIMIT;
        }
        return Outcome.CONFIRM_DISPUTE;
    }

    /** Re-checks the claim (things may have changed since the confirm prompt) and disputes when still allowed. */
    private Outcome dispute(String discordId, Profile profile) {
        Outcome outcome = claim(discordId, profile);
        if (outcome != Outcome.CONFIRM_DISPUTE) {
            return outcome;
        }
        profile.disputeBy(discordId);
        profileRepository.save(profile);
        return Outcome.DISPUTED;
    }

    private Profile openDispute(String ssid) {
        return profileRepository.findById(ssid)
                .filter(profile -> profile.getClaimState() == ProfileClaimState.DISPUTED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private ProfilePageTo page(ProfileUpdateRequest request) {
        ProfileDisputeTo disputing = profileRepository.findByDisputedByDiscordId(request.getDiscordId())
                .map(profile -> new ProfileDisputeTo(profile.getId(), profile.getRacenet(), profile.getDisputedAt()))
                .orElse(null);
        return new ProfilePageTo(editableProfile(request).map(ProfileService::toTo).orElse(null), disputing);
    }

    /** The link's profile, as long as the link's discord user still holds it. */
    private Optional<Profile> editableProfile(ProfileUpdateRequest request) {
        return Optional.ofNullable(request.getRequestedSSID())
                .flatMap(profileRepository::findById)
                .filter(profile -> profile.isHeldBy(request.getDiscordId()));
    }

    /**
     * The profile a discord user holds. Links from before claims were enforced can hold several; the one they
     * requested most recently wins.
     */
    private Optional<Profile> heldProfile(String discordId) {
        List<Profile> held = profileRepository.findByDiscordId(discordId);
        if (held.size() <= 1) {
            return held.stream().findFirst();
        }
        return updateRequestRepository.findByDiscordIdAndRequestedSSIDNotNullOrderByRequestedUpdateTimeDesc(discordId).stream()
                .map(ProfileUpdateRequest::getRequestedSSID)
                .flatMap(ssid -> held.stream().filter(profile -> profile.getId().equals(ssid)))
                .findFirst()
                .or(() -> held.stream().findFirst());
    }

    /** A link to edit the linked profile, or an unbound one on which the user confirms the dispute; else none. */
    private UUID requestFor(String discordId, Outcome outcome, Profile profile) {
        return switch (outcome) {
            case LINKED -> newRequest(discordId, profile.getId());
            case CONFIRM_DISPUTE -> newRequest(discordId, null);
            default -> null;
        };
    }

    private ProfileUpdateRequestResultTo pick(String discordId) {
        return new ProfileUpdateRequestResultTo(newRequest(discordId, null), Outcome.PICK, null, null);
    }

    private static ProfileUpdateRequestResultTo result(UUID requestId, Outcome outcome, Profile profile) {
        return new ProfileUpdateRequestResultTo(requestId, outcome, profile.getId(), profile.getRacenet());
    }

    private UUID newRequest(String discordId, String ssid) {
        return updateRequestRepository.save(new ProfileUpdateRequest(discordId, ssid)).getId();
    }

    private static ProfileTo toTo(Profile profile) {
        return new ProfileTo(
                profile.getId(),
                profile.getDisplayName(),
                profile.getController(),
                profile.getPlatform(),
                profile.getPeripheral(),
                profile.getRacenet(),
                profile.isTrackDiscord(),
                profile.getClaimState()
        );
    }
}
