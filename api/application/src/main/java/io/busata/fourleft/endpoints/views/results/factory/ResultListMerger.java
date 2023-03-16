package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.DriverResultTo;
import io.busata.fourleft.api.models.views.ActivityInfoTo;
import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.common.StageTimeParser;
import io.busata.fourleft.endpoints.club.results.service.DriverEntryToFactory;
import io.busata.fourleft.helpers.Factory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Factory
@RequiredArgsConstructor
public class ResultListMerger {

    private final StageTimeParser parser;
    private final DriverEntryToFactory driverEntryToFactory;

    public ResultListTo mergeResults(List<ResultListTo> results) {
        HashMap<String, DriverResultTo> driverEntries = new HashMap<>();

        results.stream().map(ResultListTo::results).
                forEach(entries -> {
                    entries.stream().map(DriverEntryTo::result).forEach(entry -> {
                        driverEntries.putIfAbsent(entry.racenet(), entry);
                        driverEntries.computeIfPresent(entry.racenet(), ((key, existingDriverEntry) -> mergeEntries(entry, existingDriverEntry)));
                    });
                });


        List<DriverEntryTo> driverResultTos = driverEntryToFactory.calculateRelativeData(new ArrayList<>(driverEntries.values()));

        List<ActivityInfoTo> mergedActivities = results.stream().flatMap(list -> list.activityInfoTo().stream()).distinct().toList();

        return new ResultListTo("", mergedActivities, results.size(), driverResultTos);
    }
    public DriverResultTo mergeEntries(DriverResultTo entryA, DriverResultTo entryB) {

        Duration activityTotalTime = parser.createDuration(entryA.activityTotalTime()).plus(parser.createDuration(entryB.activityTotalTime()));
        String activityTotalMerged = DurationFormatUtils.formatDuration(activityTotalTime.toMillis(), "+HH:mm:ss.SSS", true);

        Duration powerStageTotalTime = parser.createDuration(entryA.powerStageTotalTime()).plus(parser.createDuration(entryB.powerStageTotalTime()));
        String powerStageMerged = DurationFormatUtils.formatDuration(powerStageTotalTime.toMillis(), "+HH:mm:ss.SSS", true);

        return new DriverResultTo(
                entryA.racenet(),
                entryA.nationality(),
                entryA.platform(),
                activityTotalMerged,
                powerStageMerged,
                entryA.isDnf() && entryB.isDnf(),
                Stream.concat(entryA.vehicles().stream(), entryB.vehicles().stream()).distinct().collect(Collectors.toList())
        );
    }

}
