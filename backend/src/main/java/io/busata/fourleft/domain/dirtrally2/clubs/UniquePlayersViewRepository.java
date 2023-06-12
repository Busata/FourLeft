package io.busata.fourleft.domain.dirtrally2.clubs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UniquePlayersViewRepository extends JpaRepository<UniquePlayersView, String> {

    @Query(value = "SELECT name FROM unique_players where name % :query limit 10", nativeQuery = true)
    List<String> findSimilar(String query);
}
