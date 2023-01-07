package io.busata.fourleft.importer.updaters;


import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.gateway.racenet.RacenetGateway;
import io.busata.fourleft.gateway.racenet.dto.club.DR2ClubChampionships;
import io.busata.fourleft.gateway.racenet.dto.club.DR2ClubRecentResults;
import io.busata.fourleft.gateway.racenet.dto.club.championship.standings.DR2ChampionshipStandings;
import io.busata.fourleft.gateway.racenet.factory.ChampionshipFactory;
import io.busata.fourleft.gateway.racenet.factory.StandingEntryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RacenetClubSyncService {

    private final RacenetGateway racenetGateway;
    private final ChampionshipFactory championshipFactory;
    private final StandingEntryFactory standingEntryFactory;

    private final ClubRepository clubRepository;

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void syncWithRacenet(Club club) {
        syncDetails(club);
        syncChampionships(club);
        syncStandings(club);

        club.markRefreshed();

        clubRepository.save(club);
    }

    private void syncDetails(Club club) {
        final var racenetClub = racenetGateway.getClubDetails(club.getReferenceId());

        club.setName(racenetClub.club().name());
        club.setDescription(racenetClub.club().description());
        club.setMembers(racenetClub.club().memberCount());
    }

    private void syncChampionships(Club club) {
        List<Championship> baseChampionships = createBaseChampionships(club);
        List<Championship> enrichedChampionships = enrichChampionships(club.getReferenceId(), baseChampionships);

        club.merge(enrichedChampionships);
    }

    private List<Championship> createBaseChampionships(Club club) {
        DR2ClubRecentResults recentResults = racenetGateway.getClubRecentResults(club.getReferenceId());
        return recentResults.championships().stream().map(championshipFactory::create).peek(championship -> championship.setClub(club)).collect(Collectors.toList());
    }

    private List<Championship> enrichChampionships(Long clubId, List<Championship> championships) {
        List<DR2ClubChampionships> championshipDetails = racenetGateway.getChampionships(clubId);

        return championships.stream()
                .flatMap(championship ->
                        championshipDetails.stream()
                                .filter(details -> details.id().equals(championship.getReferenceId()))
                                .findFirst()
                                .map(details -> championshipFactory.enrich(championship, details))
                                .stream()
                )
                .collect(Collectors.toList());
    }

    private void syncStandings(Club club) {
        DR2ChampionshipStandings standings = racenetGateway.getClubChampionshipStandings(club.getReferenceId());
        club.findActiveChampionship().or(club::findPreviousChampionship).ifPresent(championship -> {
            championship.updateEntries(
                    standings.standings().stream()
                            .map(standingEntryFactory::create)
                            .peek(standingEntry -> standingEntry.setChampionship(championship))
                            .collect(Collectors.toList())
            );
        });
    }
}
