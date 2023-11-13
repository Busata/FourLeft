package io.busata.fourleft.backendeasportswrc.domain.services.championships;

import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ChampionshipRepository extends JpaRepository<Championship, String> {


    List<Championship> findChampionshipByClub_Id(String clubId);

    @Query("select c.standings from Championship c where c.id=:id")
    List<ChampionshipStanding> findStandings(@Param("id") String id);

}