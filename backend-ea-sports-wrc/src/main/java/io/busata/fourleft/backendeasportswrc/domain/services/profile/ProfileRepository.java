package io.busata.fourleft.backendeasportswrc.domain.services.profile;

import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ProfileRepository extends JpaRepository<Profile, String> {


    Optional<Profile> findByRacenet(String racenet);
}