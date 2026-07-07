package io.busata.fourleft.backendacrally.domain.services.club;

import io.busata.fourleft.backendacrally.domain.models.club.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, UUID> {

    List<Club> findAllByOrderByCreatedAtDesc();

    boolean existsByNameIgnoreCase(String name);
}
