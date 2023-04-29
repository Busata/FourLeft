package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.DriverResultTo;
import io.busata.fourleft.api.models.VehicleEntryTo;
import io.busata.fourleft.api.models.views.ActivityInfoTo;
import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.infrastructure.common.StageTimeParser;
import io.busata.fourleft.api.models.configuration.results.MergeMode;
import io.busata.fourleft.domain.views.configuration.results_views.RacenetFilter;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Factory
@RequiredArgsConstructor
public class ResultListMerger {

    private final StageTimeParser parser;
    private final DriverEntryToFactory driverEntryToFactory;

    public ResultListTo mergeResults(List<ResultListTo> results, MergeMode mode, RacenetFilter racenetFilter) {
        HashMap<String, DriverResultTo> driverEntries = new HashMap<>();
        HashMap<String, Long> driverEntryCount = new HashMap<>();

        results.stream().map(ResultListTo::results).
                forEach(entries -> {
                    entries.stream().map(DriverEntryTo::result).forEach(entry -> {
                        driverEntryCount.computeIfPresent(entry.racenet(), (key, existingCount) -> existingCount + 1);
                        driverEntryCount.putIfAbsent(entry.racenet(), 1L);

                        driverEntries.computeIfPresent(entry.racenet(), (key, existingDriverEntry) -> mergeEntries(mode, entry, existingDriverEntry));
                        driverEntries.putIfAbsent(entry.racenet(), entry);

                    });
                });


        List<DriverResultTo> entriesOccuringInEveryList = driverEntries.values().stream().filter(
                entry -> driverEntryCount.get(entry.racenet()) == results.size()
        ).collect(Collectors.toList());

        FilteredEntryList<DriverEntryTo> driverResultTos = driverEntryToFactory.filterResultsByFilter(entriesOccuringInEveryList, racenetFilter);


        List<ActivityInfoTo> mergedActivities = results.stream().flatMap(list -> list.activityInfoTo().stream()).distinct().toList();

        return new ResultListTo("", mergedActivities, driverResultTos.totalBeforeExclude(), driverResultTos.entries().stream().sorted(Comparator.comparing(DriverEntryTo::activityRank)).collect(Collectors.toList()));
    }
    public DriverResultTo mergeEntries(MergeMode mode, DriverResultTo entryA, DriverResultTo entryB) {

        Duration activityTotalTime = mode.merge(parser.createDuration(entryA.activityTotalTime()), parser.createDuration(entryB.activityTotalTime()));
        String activityTotalMerged = parser.formatStageTime(activityTotalTime);

        Duration powerStageTotalTime = mode.merge(parser.createDuration(entryA.powerStageTotalTime()), parser.createDuration(entryB.powerStageTotalTime()));
        String powerStageMerged = parser.formatStageTime(powerStageTotalTime);

        return new DriverResultTo(
                entryA.racenet(),
                entryA.nationality(),
                entryA.platform(),
                activityTotalMerged,
                powerStageMerged,
                entryA.isDnf() && entryB.isDnf(),
                mergeVehicleEntries(entryA, entryB)
        );
    }

    private List<VehicleEntryTo> mergeVehicleEntries(DriverResultTo entryA, DriverResultTo entryB) {
        return Stream.concat(entryA.vehicles().stream(), entryB.vehicles().stream()).collect(Collectors.toList());
    }

}
