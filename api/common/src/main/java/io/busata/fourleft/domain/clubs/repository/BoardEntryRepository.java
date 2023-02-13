package io.busata.fourleft.domain.clubs.repository;

import io.busata.fourleft.domain.clubs.models.BoardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BoardEntryRepository extends JpaRepository<BoardEntry, UUID> {

    @Query("select distinct name from BoardEntry")
    List<String> findDistinctNames();
}
