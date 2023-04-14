package io.busata.fourleft.endpoints.views.restrictions;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.configuration.event_restrictions.models.ViewEventRestrictions;
import io.busata.fourleft.domain.configuration.event_restrictions.repository.ViewEventRestrictionsRepository;
import io.busata.fourleft.domain.configuration.results_views.ResultsViewRepository;
import io.busata.fourleft.domain.options.models.Vehicle;
import io.busata.fourleft.endpoints.views.results.factory.ResultRestrictionToFactory;
import io.busata.fourleft.importer.ClubSyncService;
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
    private final ResultsViewRepository resultsViewRepository;

    private final ViewEventRestrictionsRepository viewEventRestrictionsRepository;

    private final ClubSyncService clubSyncService;

    private final ResultRestrictionToFactory resultRestrictionToFactory;

    @GetMapping(value = Routes.RESULT_VIEW_EVENT_RESTRICTION)
    public List<ResultRestrictionsTo> getViewRestrictions(@PathVariable UUID resultViewId) {
       return resultsViewRepository.findById(resultViewId).stream().flatMap(
                resultsView -> resultsView.getAssociatedClubs().stream()
        ).flatMap(clubId -> {
            Club club = clubSyncService.getOrCreate(clubId);
            return club.getCurrentEvent().map(event -> {
                return resultRestrictionToFactory.getResultRestrictionsTo(resultViewId, event);
            }).stream();
        }).toList();
    }

    @PostMapping(value= Routes.RESULT_VIEW_EVENT_RESTRICTION)
    public void createViewRestriction(@PathVariable UUID resultViewId, @RequestBody ResultRestrictionsTo resultRestrictionsTo) {
        resultsViewRepository.findById(resultViewId).ifPresent(resultsView -> {
            viewEventRestrictionsRepository.findByResultsViewIdAndChallengeIdAndEventId(
                    resultViewId, resultRestrictionsTo.getChallengeId(), resultRestrictionsTo.getEventId()
            ).ifPresentOrElse(restrictions -> {
                        ViewEventRestrictions updated = restrictions.toBuilder()
                                .vehicles(resultRestrictionsTo.getRestrictedVehicles().stream().map(
                                        vehicleTo -> {
                                            return Vehicle.valueOf(vehicleTo.id());
                                        }
                                ).toList()).build();

                        viewEventRestrictionsRepository.save(updated);
                        }, () -> {
                ViewEventRestrictions build = ViewEventRestrictions.builder()
                        .eventId(resultRestrictionsTo.getEventId())
                        .resultsView(resultsView)
                        .challengeId(resultRestrictionsTo.getChallengeId())
                        .vehicles(resultRestrictionsTo.getRestrictedVehicles().stream().map(
                                vehicleTo -> {
                                    return Vehicle.valueOf(vehicleTo.id());
                                }
                        ).toList())
                        .build();
                viewEventRestrictionsRepository.save(build);
            });

        });

    }
}
