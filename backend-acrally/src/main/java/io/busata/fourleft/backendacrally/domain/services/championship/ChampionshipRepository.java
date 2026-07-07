package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChampionshipRepository extends JpaRepository<Championship, UUID> {

    List<Championship> findAllByClubIdOrderByStartDateAsc(UUID clubId);
}
