package io.busata.fourleft.domain.players;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerInfoRepository extends JpaRepository<PlayerInfo, UUID> {

    List<PlayerInfo> findBySyncedPlatformIsFalse();
    List<PlayerInfo> findBySyncedPlatformIsTrue();

    List<PlayerInfo> findByRacenetIn(List<String> names);
}