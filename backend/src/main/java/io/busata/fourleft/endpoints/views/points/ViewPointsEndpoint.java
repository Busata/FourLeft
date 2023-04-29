package io.busata.fourleft.endpoints.views.points;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.views.configuration.ClubViewRepository;
import io.busata.fourleft.endpoints.views.points.factory.ViewPointsToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ViewPointsEndpoint {
    private final ClubViewRepository repository;

    private final ViewPointsToFactory viewPointsToFactory;


    @GetMapping(Routes.CLUB_VIEWS_CURRENT_STANDINGS_BY_VIEW_ID)
    public ViewPointsTo getCurrentPoints(@PathVariable UUID viewId) {
        final var clubView = repository.findById(viewId).orElseThrow();
        return viewPointsToFactory.create(clubView);
    }
}
