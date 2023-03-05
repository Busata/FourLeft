package io.busata.fourleft.api.models.views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.busata.fourleft.api.models.ResultEntryTo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;


@Getter
@AllArgsConstructor
public class ViewResultTo {

    private String description;

    private ViewPropertiesTo viewPropertiesTo;
    private List<ResultListTo> multiListResults;

    @JsonIgnore
    public List<ActivityInfoTo> getEventInfo() {
        return this.multiListResults.stream().flatMap(result -> getEventInfo().stream()).collect(Collectors.toList());
    }
    @JsonIgnore
    public List<ResultEntryTo> getResultEntries() {
        return multiListResults.stream().flatMap(multiList -> multiList.results().stream()).collect(Collectors.toList());
    }
}
