package io.busata.fourleft.backendeasportswrc.domain.services.profile;

import io.busata.fourleft.backendeasportswrc.domain.models.profile.ProfileUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ProfileUpdateRequestRepository extends JpaRepository<ProfileUpdateRequest, UUID> {

    List<ProfileUpdateRequest> findByDiscordIdAndRequestedSSIDNotNullOrderByRequestedUpdateTimeDesc(String discordId);
}
