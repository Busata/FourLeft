package io.busata.fourleft.domain.configuration.event_restrictions.repository;

import io.busata.fourleft.domain.configuration.event_restrictions.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    List<Player> findByTiers_Id(UUID tierId);
}