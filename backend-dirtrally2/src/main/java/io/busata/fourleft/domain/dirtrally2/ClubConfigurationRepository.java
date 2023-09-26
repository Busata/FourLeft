package io.busata.fourleft.domain.dirtrally2;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClubConfigurationRepository extends JpaRepository<ClubConfiguration, UUID> {
}