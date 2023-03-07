package io.busata.fourleft.endpoints.club.results.service;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.common.StageTimeParser;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.endpoints.views.results.factory.BoardTimeSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

import static org.apache.commons.collections4.IterableUtils.first;


@Component
@RequiredArgsConstructor
public class DriverEntryToFactory {
    private final StageTimeParser stageTimeParser;
    private final PlatformToFactory platformToFactory;

    public List<DriverEntryTo> create(List<BoardEntry> entries) {
        if (entries.isEmpty()) {
            return List.of();
        }

        final var sortedByTotalTime = sortedByTime(entries, BoardEntry::getTotalTime);
        final var sortedByPowerStageTime = sortedByTime(entries, BoardEntry::getStageTime);

        final var fastestTotalTime = first(sortedByTotalTime).getTotalTime();
        final var fastestPowerStageTime = first(sortedByPowerStageTime).getStageTime();


        return entries.stream().map(entry ->
                new DriverEntryTo(
                        entry.getName(),
                        entry.getNationality(),
                        platformToFactory.createFromRacenet(entry.getName()),
                        sortedByTotalTime.indexOf(entry) + 1L,
                        entry.getTotalTime(),
                        stageTimeParser.getDiff(fastestTotalTime, entry.getTotalTime()),
                        sortedByPowerStageTime.indexOf(entry) + 1L,
                        entry.getStageTime(),
                        stageTimeParser.getDiff(fastestPowerStageTime, entry.getStageTime()),
                        entry.isDnf(),
                        List.of(entry.getVehicleName())
                )
        ).toList();
    }

    private List<BoardEntry> sortedByTime(List<BoardEntry> entries, BoardTimeSupplier timeSupplier) {
        return entries.stream().sorted(Comparator.comparing(boardEntry -> stageTimeParser.createDuration(timeSupplier.getTime(boardEntry)))).toList();
    }

    public List<DriverEntryTo> mergeDriverEntries(List<List<DriverEntryTo>> toList) {
        return null;
    }
}
