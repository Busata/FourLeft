package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.application.dirtrally2.ClubService;
import io.busata.fourleft.domain.aggregators.restrictions.ViewEventRestrictions;
import io.busata.fourleft.domain.aggregators.restrictions.ViewEventRestrictionsRepository;
import io.busata.fourleft.domain.aggregators.results.ResultsViewRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.options.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResultRestrictionsService {
    private final ResultsViewRepository resultsViewRepository;

    private final ViewEventRestrictionsRepository viewEventRestrictionsRepository;

    private final ClubService clubService;
    private final ResultRestrictionToFactory resultRestrictionToFactory;


    public List<ResultRestrictionsTo> getViewRestrictions(UUID resultViewId) {
        return resultsViewRepository.findById(resultViewId).stream().flatMap(
                resultsView -> resultsView.getAssociatedClubs().stream()
        ).flatMap(clubId -> {
            Club club = clubService.getOrCreate(clubId);
            return club.getCurrentEvent().map(event -> {
                return resultRestrictionToFactory.getResultRestrictionsTo(resultViewId, event);
            }).stream();
        }).toList();
    }

    public void createViewRestriction(UUID resultViewId, ResultRestrictionsTo resultRestrictionsTo) {
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
