package io.busata.fourleft.application.dirtrally2.importer.clubs;


import io.busata.fourleft.infrastructure.clients.racenet.RacenetGateway;
import io.busata.fourleft.application.dirtrally2.importer.factory.ChampionshipFactory;
import io.busata.fourleft.application.dirtrally2.importer.factory.StandingEntryFactory;
import io.busata.fourleft.domain.dirtrally2.clubs.Championship;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubRepository;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubChampionships;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubRecentResults;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.standings.DR2ChampionshipStandingEntry;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.standings.DR2ChampionshipStandings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
class RacenetClubSyncService {

    private final RacenetGateway racenetGateway;
    private final ChampionshipFactory championshipFactory;
    private final StandingEntryFactory standingEntryFactory;

    private final ClubRepository clubRepository;

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
        try {

        List<DR2ChampionshipStandingEntry> entries = getStandingEntries(club);

        club.findActiveChampionship().or(club::findPreviousChampionship).ifPresent(championship -> {
            championship.updateEntries(
                    entries.stream()
                            .map(standingEntryFactory::create)
                            .peek(standingEntry -> standingEntry.setChampionship(championship))
                            .collect(Collectors.toList())
            );
        }); } catch (Exception ex) {
            log.warn("Failed to sync standings for club {}", club.getName());
        }
    }

    private List<DR2ChampionshipStandingEntry> getStandingEntries(Club club) {
        List<DR2ChampionshipStandingEntry> entries = new ArrayList<>();
        boolean keepFetching = true;
        int currentPage = 1;

        while(keepFetching) {
            DR2ChampionshipStandings standings = racenetGateway.getClubChampionshipStandings(club.getReferenceId(), currentPage);
            entries.addAll(standings.standings());

            if(currentPage >= standings.pageCount() || containsEntryWithZeroPoints(standings.standings())) {
                keepFetching = false;
            }

            currentPage += 1;
        }
        return entries;
    }

    private boolean containsEntryWithZeroPoints(List<DR2ChampionshipStandingEntry> standings) {
        return standings.stream().map(DR2ChampionshipStandingEntry::totalPoints).anyMatch(totalPoints -> totalPoints == 0);
    }
}
