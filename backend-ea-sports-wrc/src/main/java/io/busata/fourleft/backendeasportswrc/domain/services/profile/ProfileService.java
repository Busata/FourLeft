package io.busata.fourleft.backendeasportswrc.domain.services.profile;

import io.busata.fourleft.api.easportswrc.models.ProfileTo;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.ProfileUpdateRequest;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.common.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileUpdateRequestRepository updateRequestRepository;
    private final ProfileRepository profileRepository;
    private final ClubLeaderboardService leaderboardService;

    @Transactional
    public Optional<UUID> requestUpdate(String discordId, String racenet) {
        return leaderboardService.findRacenet(racenet).map(racenetInfo -> {
            return profileRepository.save(new Profile(racenetInfo.ssid(), racenetInfo.racenet(), discordId, Platform.fromEASportsWRCId(racenetInfo.platform()), true));
        }).map(profile -> {
            return updateRequestRepository.save(new ProfileUpdateRequest(discordId, profile.getId()));
        }).map(ProfileUpdateRequest::getId);
    }

    public Optional<Profile> getProfile(UUID requestId) {
        Optional<ProfileUpdateRequest> byId = updateRequestRepository.findById(requestId);

        return byId.flatMap(profileRequest -> {
            return profileRepository.findById(profileRequest.getRequestedSSID());
        });
    }

    public Profile updateProfile(UUID requestId, ProfileTo updatedData) {
        String profileId = updateRequestRepository.findById(requestId).map(ProfileUpdateRequest::getRequestedSSID).orElseThrow();

        Profile profile = profileRepository.findById(profileId).orElseThrow();

        profile.setPeripheral(updatedData.peripheral());
        profile.setPlatform(updatedData.platform());
        profile.setController(updatedData.controller());
        profile.setTrackDiscord(updatedData.trackDiscord());
        profile.setDisplayName(updatedData.displayName());


        return profileRepository.save(profile);

    }
}
