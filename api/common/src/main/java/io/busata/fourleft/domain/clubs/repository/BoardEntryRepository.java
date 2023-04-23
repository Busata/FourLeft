package io.busata.fourleft.domain.clubs.repository;

import io.busata.fourleft.domain.clubs.models.BoardEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoardEntryRepository extends JpaRepository<BoardEntry, UUID> {

}
