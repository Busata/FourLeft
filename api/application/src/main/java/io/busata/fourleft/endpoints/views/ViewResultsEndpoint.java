package io.busata.fourleft.endpoints.views;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.views.ViewEventSummaryTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.endpoints.views.points.ViewPointsToFactory;
import io.busata.fourleft.endpoints.views.results.ViewResultToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ViewResultsEndpoint {
    private final ClubViewRepository repository;
    private final ViewResultToFactory viewResultToFactory;

    private final ViewPointsToFactory viewPointsToFactory;

    private final ViewEventSummaryToFactory viewEventSummaryToFactory;

    @GetMapping(Routes.CLUB_VIEWS_CURRENT_RESULTS_BY_VIEW_ID)
    public ResponseEntity<ViewResultTo> getCurrentResults(@PathVariable UUID viewId) {
        return viewResultToFactory.createViewResult(viewId, ClubEventSupplierType.CURRENT)
                .map(results -> new ResponseEntity<>(results, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(Routes.CLUB_VIEWS_PREVIOUS_RESULTS_BY_VIEW_ID)
    public ResponseEntity<ViewResultTo> getPreviousResults(@PathVariable UUID viewId) {
        return viewResultToFactory.createViewResult(viewId, ClubEventSupplierType.PREVIOUS)
                .map(results -> new ResponseEntity<>(results, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(Routes.CLUB_VIEWS_CURRENT_STANDINGS_BY_VIEW_ID)
    public ViewPointsTo getCurrentPoints(@PathVariable UUID viewId) {
        final var clubView = repository.findById(viewId).orElseThrow();
        return viewPointsToFactory.create(clubView);
    }


    @GetMapping(Routes.CLUB_VIEWS_EVENT_SUMMARY_BY_VIEW_ID)
    public ViewEventSummaryTo getEventSummary(@PathVariable UUID viewId) {
        final var clubView = repository.findById(viewId).orElseThrow();
        return viewEventSummaryToFactory.create(clubView);
    }

}
