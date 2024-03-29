package io.busata.fourleft.endpoints.aggregators;

import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.views.ViewEventSummaryTo;
import io.busata.fourleft.application.dirtrally2.aggregators.ViewSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ViewSummaryEndpoint {
    private final ViewSummaryService viewSummaryService;

    @GetMapping(RoutesTo.CLUB_VIEWS_EVENT_SUMMARY_BY_VIEW_ID)
    public ViewEventSummaryTo getEventSummary(@PathVariable UUID viewId) {
       return viewSummaryService.getEventSummary(viewId);
    }

}
