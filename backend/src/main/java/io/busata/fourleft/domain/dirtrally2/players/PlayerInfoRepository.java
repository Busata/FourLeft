package io.busata.fourleft.domain.dirtrally2.players;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerInfoRepository extends JpaRepository<PlayerInfo, UUID> {

    List<PlayerInfo> findByRacenetIn(List<String> names);
    List<PlayerInfo> findBySyncedPlatformIsFalseAndRacenetIn(List<String> names);
    Optional<PlayerInfo> findByRacenet(String name);


    @Query("select p from PlayerInfo p where p.createdBeforeRacenetChange = true")
    List<PlayerInfo> findByCreatedBeforeRacenetChange();

    @Query("select p from PlayerInfo p where p.racenet = :name and p.createdBeforeRacenetChange = true")
    Optional<PlayerInfo> findByRacenetAndCreatedBeforeRacenetChange(String name);
    
}
