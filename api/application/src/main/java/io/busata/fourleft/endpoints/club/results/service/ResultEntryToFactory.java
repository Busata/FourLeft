package io.busata.fourleft.endpoints.club.results.service;

import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.common.StageTimeParser;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.players.PlayerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.busata.fourleft.api.models.ResultEntryTo.resultEntryTo;


@Component
@RequiredArgsConstructor
public class ResultEntryToFactory {
    private final StageTimeParser stageTimeParser;
    private final PlayerInfoRepository playerInfoRepository;

    public List<ResultEntryTo> create(List<BoardEntry> entries) {
        if(entries.isEmpty()) {
            return List.of();
        }

        final String fastestTotalTime = getFastestTotalTime(entries);

        final var sortedByPowerStageTime = entries.stream().sorted(compareByStageTime()).toList();
        final var fastestPowerStageTime = sortedByPowerStageTime.get(0).getStageTime();

        final var buildersSortedByTotalTime = IntStream.range(0, sortedByPowerStageTime.size()).mapToObj(index -> {
            BoardEntry entry = sortedByPowerStageTime.get(index);

            ResultEntryTo.ResultEntryToBuilder resultEntryToBuilder = resultEntryTo();
            playerInfoRepository.findByRacenet(entry.getName()).ifPresent(playerInfo -> {
                resultEntryToBuilder.platform(playerInfo.getPlatform());
                resultEntryToBuilder.controllerType(playerInfo.getController());
            });

            return resultEntryToBuilder
                    .name(entry.getName())
                    .nationality(entry.getNationality())
                    .vehicle(entry.getVehicleName())
                    .totalTime(entry.getTotalTime())
                    .totalDiff(stageTimeParser.getDiff(fastestTotalTime, entry.getTotalTime()))
                    .stageTime(entry.getStageTime())
                    .stageRank(index + 1)
                    .stageDiff(stageTimeParser.getDiff(fastestPowerStageTime, entry.getStageTime()))
                    .isDnf(entry.isDnf());
        }).sorted(Comparator.comparing(builder -> stageTimeParser.createDuration(builder.getTotalTime()))).toList();

        return IntStream.range(0, buildersSortedByTotalTime.size()).mapToObj(index -> {
            ResultEntryTo.ResultEntryToBuilder builder = buildersSortedByTotalTime.get(index);

            return builder.rank((long) index + 1).build();
        }).sorted(Comparator.comparing(ResultEntryTo::rank)).collect(Collectors.toList());
    }



    private String getFastestTotalTime(List<BoardEntry> filteredByTier) {
        return filteredByTier.stream().min(Comparator.comparing(boardEntry -> stageTimeParser.createDuration(boardEntry.getTotalTime()))).map(BoardEntry::getTotalTime).orElseThrow();
    }
    private Comparator<BoardEntry> compareByStageTime() {
        return Comparator.comparing(boardEntry -> stageTimeParser.createDuration(boardEntry.getStageTime()));
    }
}
