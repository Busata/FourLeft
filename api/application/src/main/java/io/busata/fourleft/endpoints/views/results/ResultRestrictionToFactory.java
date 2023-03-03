package io.busata.fourleft.endpoints.views.results;

import io.busata.fourleft.api.models.views.NoResultRestrictionsTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.api.models.views.VehicleTo;
import io.busata.fourleft.domain.configuration.event_restrictions.models.ViewEventRestrictions;
import io.busata.fourleft.domain.configuration.event_restrictions.repository.ViewEventRestrictionsRepository;
import io.busata.fourleft.domain.options.models.Vehicle;
import io.busata.fourleft.helpers.Factory;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Factory
@RequiredArgsConstructor
public class ResultRestrictionToFactory {
    private final ViewEventRestrictionsRepository viewEventRestrictionsRepository;


    public ResultRestrictionsTo create(UUID resultViewId, String challengeId, String eventId) {
        final var restrictions = viewEventRestrictionsRepository.findByResultViewIdAndChallengeIdAndEventId(resultViewId, challengeId, eventId);

        return restrictions.map(this::create).orElse(new NoResultRestrictionsTo());
    }

    private  ResultRestrictionsTo create(ViewEventRestrictions viewEventRestrictions) {
        return new ResultListRestrictionsTo(viewEventRestrictions.getVehicles().stream().map(
                this::createVehicle).toList());

    }
    private VehicleTo createVehicle(Vehicle vehicle) {
        return new VehicleTo(
                vehicle.name(),
                vehicle.getDisplayName()
        );
    }
}
