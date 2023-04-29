package io.busata.fourleft.domain.aggregators.repository;

import io.busata.fourleft.domain.aggregators.ClubConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClubConfigurationRepository extends JpaRepository<ClubConfiguration, UUID> {
}