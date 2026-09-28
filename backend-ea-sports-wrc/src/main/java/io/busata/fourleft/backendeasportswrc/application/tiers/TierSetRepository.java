package io.busata.fourleft.backendeasportswrc.application.tiers;

import io.busata.fourleft.backendeasportswrc.domain.models.tiers.TierSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface TierSetRepository extends JpaRepository<TierSet, UUID> {

    List<TierSet> findByGuildIdOrderByNameAsc(Long guildId);

    Optional<TierSet> findByGuildIdAndNameIgnoreCase(Long guildId, String name);
}
