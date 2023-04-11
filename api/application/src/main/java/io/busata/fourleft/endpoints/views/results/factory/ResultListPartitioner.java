package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.DriverResultTo;
import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.domain.configuration.results_views.PartitionElement;
import io.busata.fourleft.helpers.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Factory
@RequiredArgsConstructor
public class ResultListPartitioner {
    private final DriverEntryToFactory factory;

    public List<ResultListTo> partitionResults(List<PartitionElement> partitions, ResultListTo resultList) {
        return partitions.stream().map(partition -> {
            final var racenetNames = partition.getRacenetNames();

            List<DriverResultTo> partitionedList = resultList.results().stream()
                    .map(DriverEntryTo::result)
                    .filter(driverEntryTo -> racenetNames.contains(driverEntryTo.racenet()))
                    .collect(Collectors.toList());


            List<DriverEntryTo> driverEntryTos = factory.calculateRelativeData(partitionedList);

            return new ResultListTo(partition.getName(), resultList.activityInfoTo(), driverEntryTos.size(), driverEntryTos);
        }).collect(Collectors.toList());
    }

}
