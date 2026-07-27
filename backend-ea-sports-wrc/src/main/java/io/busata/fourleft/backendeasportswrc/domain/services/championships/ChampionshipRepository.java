package io.busata.fourleft.backendeasportswrc.domain.services.championships;

import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ChampionshipRepository extends JpaRepository<Championship, String> {


    List<Championship> findChampionshipByClub_Id(String clubId);

    // Entity query, not "select c.standings from Championship c": projecting the collection makes
    // Hibernate derive the eager Championship.events SUBSELECT fetch from a query that has no usable
    // owner table group, which NPEs during loading.
    @Query("select s from ChampionshipStanding s where s.championship.id=:id")
    List<ChampionshipStanding> findStandings(@Param("id") String id);

}