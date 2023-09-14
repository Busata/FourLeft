package io.busata.fourleft.domain.dirtrally2.clubs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BoardEntryRepository extends JpaRepository<BoardEntry, UUID> {


    @Query(value = "update board_entry be set player_info_id=:newId where player_info_id=:oldId", nativeQuery = true)
    @Modifying
    void replaceObsolete(@Param("newId") UUID newPlayerInfoId, @Param("oldId") UUID oldPlayerInfoId);

    @Query(value = "update board_entry be set player_info_id=:player_info_id where name=:racenet", nativeQuery = true)
    @Modifying
    void updatePlayerInfo(@Param("racenet") String racenet, @Param("player_info_id") UUID id);
}
