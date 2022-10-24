package io.busata.fourleft.domain.clubs.repository;

import io.busata.fourleft.domain.clubs.models.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByReferenceId(Long clubId);

    Club getByReferenceId(Long clubId);
    Optional<Club> findByReferenceId(Long clubId);
}
