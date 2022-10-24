package io.busata.fourleftdiscord.autoposting.club_results.model;

import io.busata.fourleft.api.models.views.SingleResultListTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AutoPostableFactory {

    public AutoPostableView create(ViewResultTo result, List<String> entries) {

        return new AutoPostableView(
                result.getViewPropertiesTo(),
                result.getMultiListResults().stream().map(multiListResults -> {
                    return new SingleResultListTo(
                            multiListResults.name(),
                            multiListResults.eventInfoTo(),
                            multiListResults.restrictions(),
                            multiListResults.results().stream().filter(resultEntryTo -> entries.contains(resultEntryTo.name())).collect(Collectors.toList())
                    );
                }).collect(Collectors.toList())

        );
    }
}
