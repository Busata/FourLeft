package io.busata.fourleft.endpoints.aggregators;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.application.aggregators.ResultRestrictionsService;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Club;
import io.busata.fourleft.domain.aggregators.restrictions.ViewEventRestrictions;
import io.busata.fourleft.domain.aggregators.restrictions.ViewEventRestrictionsRepository;
import io.busata.fourleft.domain.aggregators.results_views.ResultsViewRepository;
import io.busata.fourleft.domain.dirtrally2.options.models.Vehicle;
import io.busata.fourleft.application.aggregators.ResultRestrictionToFactory;
import io.busata.fourleft.application.dirtrally2.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ViewEventRestrictionsEndpoint {

    private final ResultRestrictionsService resultRestrictionsService;

    @GetMapping(value = Routes.RESULT_VIEW_EVENT_RESTRICTION)
    public List<ResultRestrictionsTo> getViewRestrictions(@PathVariable UUID resultViewId) {
       return resultRestrictionsService.getViewRestrictions(resultViewId);
    }

    @PostMapping(value= Routes.RESULT_VIEW_EVENT_RESTRICTION)
    public void createViewRestriction(@PathVariable UUID resultViewId, @RequestBody ResultRestrictionsTo resultRestrictionsTo) {
       resultRestrictionsService.createViewRestriction(resultViewId, resultRestrictionsTo);

    }
}
