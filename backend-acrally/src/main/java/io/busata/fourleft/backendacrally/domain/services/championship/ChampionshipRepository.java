package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChampionshipRepository extends JpaRepository<Championship, UUID> {

    List<Championship> findAllByClubIdOrderByStartsAtAsc(UUID clubId);

    /** Published championships across a set of clubs — the driver's enterable seasons. */
    List<Championship> findAllByClubIdInAndStatus(Collection<UUID> clubIds, ChampionshipStatus status);
}
