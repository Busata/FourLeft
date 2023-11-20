package io.busata.fourleft.backendeasportswrc.domain.services.leaderboards;

import io.busata.fourleft.backendeasportswrc.application.importer.LeaderboardFactory;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardUpdatedResult;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboard;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.projections.RacenetInfo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardEntryTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper.parseDuration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubLeaderboardService {

    private final ClubService clubService;

    private final LeaderboardFactory leaderboardFactory;
    private final ClubLeaderboardRepository clubLeaderboardRepository;
    private final ClubLeaderboardEntryRepository clubLeaderboardEntryRepository;


    @Transactional(readOnly = true)
    public Optional<RacenetInfo> findRacenet(String racenet) {
        return this.clubLeaderboardEntryRepository.findRacenet(racenet).findFirst().map(clubLeaderboardEntry -> {
            return new RacenetInfo(clubLeaderboardEntry.getSsid(), clubLeaderboardEntry.getDisplayName(), clubLeaderboardEntry.getPlatform());
        });
    }

    @Transactional
    public void updateLeaderboards(LeaderboardUpdatedResult list) {

        if(list.getEntries().isEmpty()) {
            log.warn("Results for Club {} was empty", list.getClubId());
        }

        ClubLeaderboard clubLeaderboard = clubLeaderboardRepository.findById(list.getLeaderboardId()).orElse(new ClubLeaderboard(list.getLeaderboardId(), list.getEntries().size()));

        List<ClubLeaderboardEntryTo> uniqueEntriesSortedByAccumulatedTime = list.getEntries().stream().collect(Collectors.toMap(ClubLeaderboardEntryTo::displayName, e -> e, (e, v) -> e)).values().stream().sorted(Comparator.comparing(entry -> parseDuration(entry.timeAccumulated()).toNanos())).collect(Collectors.toList());

        clubLeaderboard.updateEntries(calculateCumulativeProperties(uniqueEntriesSortedByAccumulatedTime));

        clubLeaderboard.setTotalEntries(uniqueEntriesSortedByAccumulatedTime.size());


        clubLeaderboardRepository.save(clubLeaderboard);

        clubService.markBoardAsUpdated(list.getLeaderboardId());
    }

    private List<ClubLeaderboardEntry> calculateCumulativeProperties(List<ClubLeaderboardEntryTo> uniqueEntriesSortedByAccumulatedTime) {
        if (uniqueEntriesSortedByAccumulatedTime.isEmpty()) {
            return List.of();
        }

        ClubLeaderboardEntryTo firstResult = uniqueEntriesSortedByAccumulatedTime.get(0);
        Duration firstTimeAccumulated = parseDuration(firstResult.timeAccumulated());


        List<ClubLeaderboardEntry> leaderboardEntries = new ArrayList<>();

        for (int i = 0; i < uniqueEntriesSortedByAccumulatedTime.size(); i++) {

            ClubLeaderboardEntryTo clubLeaderboardEntryTo = uniqueEntriesSortedByAccumulatedTime.get(i);
            Duration timeAccumulated = parseDuration(clubLeaderboardEntryTo.timeAccumulated());

            ClubLeaderboardEntry entry = leaderboardFactory.createEntry(clubLeaderboardEntryTo, i + 1L, timeAccumulated.minus(firstTimeAccumulated));
            leaderboardEntries.add(entry);
        }

        return leaderboardEntries;
    }


    @Transactional(readOnly = true)
    public ClubLeaderboard findById(String boardId) {
        return this.clubLeaderboardRepository.findById(boardId).orElseThrow();
    }


    @Transactional(readOnly = true)
    public List<ClubLeaderboardEntry> findEntries(String leaderboardId) {

        return this.clubLeaderboardRepository.findEntries(leaderboardId);
    }
}
