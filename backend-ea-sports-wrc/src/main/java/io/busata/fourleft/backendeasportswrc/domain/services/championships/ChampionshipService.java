package io.busata.fourleft.backendeasportswrc.domain.services.championships;

import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChampionshipService {

    private final ChampionshipRepository repository;

    @Transactional(readOnly = true)
    public List<Championship> findChampionshipsByClubId(String clubId) {
        return repository.findChampionshipByClub_Id(clubId);
    }

    @Transactional(readOnly = true)
    public List<ChampionshipStanding> findStandings(String championshipId) {
        return repository.findStandings(championshipId);
    }

    @Transactional(readOnly = true)
    public Optional<Championship> findChampionship(String championshipId) {
        return repository.findById(championshipId);
    }

    public void save(Championship championship) {
        this.repository.save(championship);
    }
}
