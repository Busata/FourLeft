package io.busata.fourleft.endpoints.aggregators;

import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.application.aggregators.ResultRestrictionsService;
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

    @GetMapping(value = RoutesTo.RESULT_VIEW_EVENT_RESTRICTION)
    public List<ResultRestrictionsTo> getViewRestrictions(@PathVariable UUID resultViewId) {
       return resultRestrictionsService.getViewRestrictions(resultViewId);
    }

    @PostMapping(value= RoutesTo.RESULT_VIEW_EVENT_RESTRICTION)
    public void createViewRestriction(@PathVariable UUID resultViewId, @RequestBody ResultRestrictionsTo resultRestrictionsTo) {
       resultRestrictionsService.createViewRestriction(resultViewId, resultRestrictionsTo);

    }
}
