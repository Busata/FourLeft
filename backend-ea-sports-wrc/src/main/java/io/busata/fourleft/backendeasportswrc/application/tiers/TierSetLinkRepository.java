package io.busata.fourleft.backendeasportswrc.application.tiers;

import io.busata.fourleft.backendeasportswrc.domain.models.tiers.TierSetLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface TierSetLinkRepository extends JpaRepository<TierSetLink, UUID> {
}
