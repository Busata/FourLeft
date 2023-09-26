package io.busata.fourleft.endpoints.aggregators;

import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.application.dirtrally2.aggregators.ViewResultsService;
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
    private final ViewResultsService viewResultsService;

    @GetMapping(RoutesTo.CLUB_VIEWS_CURRENT_RESULTS_BY_VIEW_ID)
    public ResponseEntity<ViewResultTo> getCurrentResults(@PathVariable UUID viewId) {
        return viewResultsService.getCurrentResults(viewId).map(results -> new ResponseEntity<>(results, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(RoutesTo.CLUB_VIEWS_PREVIOUS_RESULTS_BY_VIEW_ID)
    public ResponseEntity<ViewResultTo> getPreviousResults(@PathVariable UUID viewId) {
        return viewResultsService.getPreviousResults(viewId).map(results -> new ResponseEntity<>(results, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }
}
