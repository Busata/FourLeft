package io.busata.fourleft.domain.dirtrally2.players;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerInfoRepository extends JpaRepository<PlayerInfo, UUID> {

    @Query(value = "select * from player_info pi where pi.id in (select distinct player_info_id as id from player_info_racenets pir where pir.racenets = :name)", nativeQuery = true)
    Optional<PlayerInfo> findByRacenet(@Param("name") String name);

}
