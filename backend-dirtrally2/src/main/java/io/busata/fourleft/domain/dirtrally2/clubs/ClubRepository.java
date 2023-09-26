package io.busata.fourleft.domain.dirtrally2.clubs;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Club getByReferenceId(Long clubId);

    @Transactional()
    Optional<Club> findByReferenceId(Long clubId);
}
