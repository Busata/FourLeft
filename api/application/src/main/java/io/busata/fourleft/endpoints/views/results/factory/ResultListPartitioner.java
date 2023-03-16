package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.DriverResultTo;
import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.common.StageTimeParser;
import io.busata.fourleft.domain.configuration.results_views.PartitionElement;
import io.busata.fourleft.helpers.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Factory
@RequiredArgsConstructor
public class ResultListPartitioner {

    private final StageTimeParser parser;


    public List<ResultListTo> partitionResults(List<PartitionElement> partitions, ResultListTo resultList) {
        return partitions.stream().map(partition -> {
            final var filter = partition.getFilter();


            List<DriverResultTo> collect = resultList.results().stream().filter(driverEntryTo -> filter.getPlayerNames().contains(driverEntryTo.racenet())).collect(Collectors.toList());



            return new ResultListTo(partition.getName(), resultList.activityInfoTo(), 0, null);



        }).collect(Collectors.toList());
    }

}
