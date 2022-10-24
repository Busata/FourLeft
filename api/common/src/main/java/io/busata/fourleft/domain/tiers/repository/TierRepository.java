package io.busata.fourleft.domain.tiers.repository;

import io.busata.fourleft.domain.tiers.models.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TierRepository extends JpaRepository<Tier, UUID> {

    List<Tier> findByClubId(Long clubId);

}