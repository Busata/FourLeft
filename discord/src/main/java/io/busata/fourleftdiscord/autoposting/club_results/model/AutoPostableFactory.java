package io.busata.fourleftdiscord.autoposting.club_results.model;

import io.busata.fourleft.api.models.views.ViewResultTo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AutoPostableFactory {

    public AutoPostableView create(ViewResultTo result, List<String> entries) {

        return new AutoPostableView(
                result.getViewEventKey(),
                result.getViewPropertiesTo(),
                result.getMultiListResults().stream().map(multiListResults -> {
                    return new AutoPostResultList(
                            multiListResults.name(),
                            multiListResults.activityInfoTo(),
                            multiListResults.totalUniqueEntries(),
                            multiListResults.results().stream().filter(resultEntryTo -> entries.contains(resultEntryTo.racenet())).collect(Collectors.toList())
                    );
                }).collect(Collectors.toList())

        );
    }
}
