package io.busata.fourleft.domain.dirtrally2.clubs.repository;

import io.busata.fourleft.domain.dirtrally2.clubs.models.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByReferenceId(Long clubId);

    Club getByReferenceId(Long clubId);

    @Transactional()
    Optional<Club> findByReferenceId(Long clubId);
}
