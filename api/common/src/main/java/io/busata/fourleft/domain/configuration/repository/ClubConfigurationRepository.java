package io.busata.fourleft.domain.configuration.repository;

import io.busata.fourleft.domain.configuration.ClubConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClubConfigurationRepository extends JpaRepository<ClubConfiguration, UUID> {
}