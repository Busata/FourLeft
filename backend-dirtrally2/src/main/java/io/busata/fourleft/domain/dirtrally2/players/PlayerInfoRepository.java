package io.busata.fourleft.domain.dirtrally2.players;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerInfoRepository extends JpaRepository<PlayerInfo, UUID> {

    @Query(value = "select * from player_info pi where pi.id in (select distinct player_info_id as id from player_info_aliases pia where pia.aliases = :name) or pi.racenet=:name", nativeQuery = true)
    Optional<PlayerInfo> findByRacenetOrAliases(@Param("name") String name);

    @Query(value = "select pi from PlayerInfo pi where pi.racenet=:racenet")
    Optional<PlayerInfo> findByRacenet(@Param("racenet") String racenet);


    @Query(value = "select pi from PlayerInfo pi where pi.trackCommunity=true")
    List<PlayerInfo> findTrackedPlayers();

}
