package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.api.models.views.VehicleTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.event_restrictions.models.ViewEventRestrictions;
import io.busata.fourleft.domain.configuration.event_restrictions.repository.ViewEventRestrictionsRepository;
import io.busata.fourleft.domain.options.models.Vehicle;
import io.busata.fourleft.helpers.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Factory
@RequiredArgsConstructor
public class ResultRestrictionToFactory {
    private final ViewEventRestrictionsRepository viewEventRestrictionsRepository;

    public ResultRestrictionsTo getResultRestrictionsTo(UUID resultViewId, Event event) {
        return viewEventRestrictionsRepository.findByResultsViewIdAndChallengeIdAndEventId(resultViewId,
                event.getChallengeId(), event.getReferenceId()).map(existingRestrictions -> {
            return create(event, existingRestrictions);
        }).orElse(new ResultRestrictionsTo(null, event.getVehicleClass(), resultViewId, event.getChallengeId(), event.getReferenceId(), List.of()));
    }

    public ResultRestrictionsTo create(Event event, ViewEventRestrictions viewEventRestrictions) {
        return new ResultRestrictionsTo(viewEventRestrictions.getId(), event.getVehicleClass(),viewEventRestrictions.getResultsView().getId(), viewEventRestrictions.getChallengeId(), viewEventRestrictions.getEventId(),  viewEventRestrictions.getVehicles().stream().map(
                this::createVehicle).toList());

    }
    private VehicleTo createVehicle(Vehicle vehicle) {
        return new VehicleTo(
                vehicle.name(),
                vehicle.getDisplayName()
        );
    }
}
