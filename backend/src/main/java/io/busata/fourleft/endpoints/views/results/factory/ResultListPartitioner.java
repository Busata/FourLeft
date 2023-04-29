package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.domain.views.configuration.results_views.RacenetFilter;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Factory
@RequiredArgsConstructor
public class ResultListPartitioner {
    private final DriverEntryToFactory factory;

    public List<ResultListTo> partitionResults(List<RacenetFilter> partitions, ResultListTo resultList) {
        return partitions.stream().filter(RacenetFilter::isEnabled).map(partition -> {

            FilteredEntryList<DriverEntryTo> filteredList = factory.filterResultsByFilter(resultList.results().stream().map(DriverEntryTo::result).toList(), partition);

            return new ResultListTo(partition.getName(), resultList.activityInfoTo(), filteredList.totalBeforeExclude(), filteredList.entries());
        }).collect(Collectors.toList());
    }

}
