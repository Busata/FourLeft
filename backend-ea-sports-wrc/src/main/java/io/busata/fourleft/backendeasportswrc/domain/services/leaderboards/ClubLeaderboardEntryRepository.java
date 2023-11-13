package io.busata.fourleft.backendeasportswrc.domain.services.leaderboards;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

interface ClubLeaderboardEntryRepository extends JpaRepository<ClubLeaderboardEntry, UUID> {


    @Query("select cle from ClubLeaderboardEntry cle where cle.displayName like CONCAT('%',:racenet,'%')")
    Stream<ClubLeaderboardEntry> findRacenet(@Param("racenet") String racenet);

}