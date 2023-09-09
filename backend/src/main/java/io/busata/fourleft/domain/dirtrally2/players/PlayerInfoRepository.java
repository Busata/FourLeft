package io.busata.fourleft.domain.dirtrally2.players;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerInfoRepository extends JpaRepository<PlayerInfo, UUID> {

    @Query("select p from PlayerInfo p where :racenet member of p.racenets")
    Optional<PlayerInfo> findByRacenet(@Param("racenet") String name);

}
