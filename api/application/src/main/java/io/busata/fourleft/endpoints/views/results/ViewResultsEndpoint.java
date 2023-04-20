package io.busata.fourleft.endpoints.views.results;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.messages.LeaderboardUpdated;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.endpoints.views.ClubEventSupplier;
import io.busata.fourleft.endpoints.views.results.factory.ViewResultToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ViewResultsEndpoint {
    private final ViewResultToFactory viewResultToFactory;

    @GetMapping(Routes.CLUB_VIEWS_CURRENT_RESULTS_BY_VIEW_ID)
    public ResponseEntity<ViewResultTo> getCurrentResults(@PathVariable UUID viewId) {
        return viewResultToFactory.createViewResult(viewId, ClubEventSupplier.CURRENT)
                .map(results -> new ResponseEntity<>(results, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(Routes.CLUB_VIEWS_PREVIOUS_RESULTS_BY_VIEW_ID)
    public ResponseEntity<ViewResultTo> getPreviousResults(@PathVariable UUID viewId) {
        return viewResultToFactory.createViewResult(viewId, ClubEventSupplier.PREVIOUS)
                .map(results -> new ResponseEntity<>(results, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }
}
