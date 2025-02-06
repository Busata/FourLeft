package io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubConfigurationRepository extends JpaRepository<ClubConfiguration, Long> {

    @Query("select cc from ClubConfiguration cc where cc.clubId=:clubId")
    List<ClubConfiguration> findByClubId(@Param("clubId") String clubId);

    @Query("update ClubConfiguration cc set cc.keepSynced=true")
    @Modifying
    void resetDisabledClubs();
}