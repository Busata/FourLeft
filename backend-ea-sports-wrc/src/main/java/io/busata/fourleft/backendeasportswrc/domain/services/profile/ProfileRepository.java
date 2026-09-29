package io.busata.fourleft.backendeasportswrc.domain.services.profile;

import io.busata.fourleft.api.easportswrc.models.ProfileClaimState;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ProfileRepository extends JpaRepository<Profile, String> {


    Optional<Profile> findByRacenet(String racenet);

    List<Profile> findByDiscordId(String discordId);

    boolean existsByDisputedByDiscordId(String discordId);

    Optional<Profile> findByDisputedByDiscordId(String discordId);

    List<Profile> findByClaimStateOrderByDisputedAtAsc(ProfileClaimState claimState);
}
