package io.busata.fourleft.backendeasportswrc.domain.services.clubExport;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubExportConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubExportConfigurationRepository extends JpaRepository<ClubExportConfiguration, Long> {

    @Query("select ce from ClubExportConfiguration ce where ce.clubId=:clubId")
    Optional<ClubExportConfiguration> findByClubId(@Param("clubId") String clubId);

    @Query("select ce from ClubExportConfiguration ce where ce.enabled=true")
    List<ClubExportConfiguration> findAllEnabled();
}
