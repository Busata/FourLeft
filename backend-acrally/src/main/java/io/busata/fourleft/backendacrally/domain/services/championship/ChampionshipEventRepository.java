package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChampionshipEventRepository extends JpaRepository<ChampionshipEvent, UUID> {

    List<ChampionshipEvent> findAllByChampionshipIdOrderByPositionAsc(UUID championshipId);

    long countByChampionshipId(UUID championshipId);
}
