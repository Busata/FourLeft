package io.busata.fourleft.endpoints.aggregators;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.application.aggregators.ViewPointsService;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import io.busata.fourleft.application.aggregators.ViewPointsToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ViewPointsEndpoint {
    private final ViewPointsService viewPointsService;

    @GetMapping(Routes.CLUB_VIEWS_CURRENT_STANDINGS_BY_VIEW_ID)
    public ViewPointsTo getCurrentPoints(@PathVariable UUID viewId) {
        return viewPointsService.getCurrentPoints(viewId);
    }
}
