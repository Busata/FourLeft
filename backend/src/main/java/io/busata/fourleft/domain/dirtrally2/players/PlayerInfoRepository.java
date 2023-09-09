package io.busata.fourleft.domain.dirtrally2.players;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerInfoRepository extends JpaRepository<PlayerInfo, UUID> {

    @Query("Select p from PlayerInfo p where :name in p.racenets")
    Optional<PlayerInfo> findByRacenet(String name);

}
