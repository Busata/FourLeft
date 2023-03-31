package io.busata.fourleft.endpoints.club.results.service;

import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.api.models.CustomChampionshipStandingEntryTo;
import io.busata.fourleft.importer.ClubSyncService;
import io.busata.fourleft.common.StageTimeParser;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomChampionshipStandingsService {

    private final ClubSyncService clubSyncService;
    private final LeaderboardRepository leaderboardRepository;

    private final JRCPointsCalculator pointsCalculator;

    private final StageTimeParser stageTimeParser;


    public List<CustomChampionshipStandingEntryTo> getEntries(Long clubId, int championshipCycle, boolean powerStageSystem) {
        Club club = clubSyncService.getOrCreate(clubId);

        Map<String, Long> pointsPerPlayer = new HashMap<>();
        Map<String, Long> powerPointsPerPlayer = new HashMap<>();
        Map<String, String> nationality = new HashMap<>();

        getEvents(clubId, championshipCycle)
                .forEach(event -> {
                    log.info("Event start: {} ", event.getStartTime());
                    final var lastStage = event.getLastStage();
                    final var entries = leaderboardRepository.findLeaderboardByChallengeIdAndEventIdAndStageId(event.getChallengeId(), event.getReferenceId(), String.valueOf(lastStage.getReferenceId()))
                            .map(board -> board.getEntries().stream().sorted(Comparator.comparing(BoardEntry::getRank)).collect(Collectors.toList()))
                            .orElse(new ArrayList<>());

                    entries.forEach(entry -> {
                        nationality.putIfAbsent(entry.getName(), entry.getNationality());
                    });


                    if(powerStageSystem) {
                        AtomicInteger powerStageRank = new AtomicInteger(0);

                        entries.stream()
                                .sorted(Comparator.comparing(boardEntry -> stageTimeParser.createDuration(boardEntry.getStageTime())))
                                .limit(5).filter(BoardEntry::hasFinished).forEach(entry -> {

                                    powerPointsPerPlayer.putIfAbsent(entry.getName(), 0L);
                                    powerPointsPerPlayer.computeIfPresent(
                                            entry.getName(),
                                            (key, value) -> value + pointsCalculator.calculatePowerStagePoints(powerStageRank.addAndGet(1))
                                    );

                                    pointsPerPlayer.compute(
                                            entry.getName(),
                                            (key, value) -> value == null ? pointsCalculator.calculatePowerStagePoints(powerStageRank.get()) : value + pointsCalculator.calculatePowerStagePoints(powerStageRank.get()));
                                });
                    }


                    entries.stream().filter(BoardEntry::hasFinished).forEach(entry -> {
                        pointsPerPlayer.putIfAbsent(entry.getName(), 0L);
                        pointsPerPlayer.computeIfPresent(
                                entry.getName(),
                                (key, value) -> value + pointsCalculator.calculateEventPoints((int) entry.getRank()));
                    });

                });

        AtomicLong rank = new AtomicLong(0);
        return pointsPerPlayer.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new CustomChampionshipStandingEntryTo(rank.addAndGet(1), nationality.get(entry.getKey()), entry.getKey(), entry.getValue(), powerPointsPerPlayer.get(entry.getKey()))).collect(Collectors.toList());
    }

    public Stream<Event> getEvents(Long clubId, int championshipCycle) {
        Club club = clubSyncService.getOrCreate(clubId);

        int currentCycle = (int) club.getChampionships().stream().dropWhile(championship -> {
            if(clubId.equals(431561L)) {
                return !(championship.getEvents().get(0).getStartTime().getDayOfWeek().equals(DayOfWeek.SUNDAY)
                        || championship.getEvents().get(0).getStartTime().getDayOfWeek().equals(DayOfWeek.MONDAY));
            }

            return !championship.getEvents().get(0).getStartTime().getDayOfWeek().equals(DayOfWeek.MONDAY);
        }).filter(Championship::isInActive).count() % championshipCycle;
        int limit = currentCycle == 0 ? championshipCycle : currentCycle;
        log.info("Building championship overview , limited to last {} championships", limit);
        return club.getChampionships()
                .stream()
                .dropWhile(championship -> {
                    if(clubId.equals(431561L)) {
                        return !(championship.getEvents().get(0).getStartTime().getDayOfWeek().equals(DayOfWeek.SUNDAY)
                                || championship.getEvents().get(0).getStartTime().getDayOfWeek().equals(DayOfWeek.MONDAY));
                    }

                    return !championship.getEvents().get(0).getStartTime().getDayOfWeek().equals(DayOfWeek.MONDAY);
                }).sorted(Comparator.comparing(Championship::getOrder).reversed())
                .filter(Championship::isInActive)
                .limit(limit)
                .flatMap(championship -> championship.getEvents().stream());
    }

}
