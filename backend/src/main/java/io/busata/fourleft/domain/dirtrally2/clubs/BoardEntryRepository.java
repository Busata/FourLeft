package io.busata.fourleft.domain.dirtrally2.clubs;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoardEntryRepository extends JpaRepository<BoardEntry, UUID> {

}
