package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.views.ActivityInfoTo;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import io.busata.fourleft.helpers.Factory;

import java.util.UUID;

@Factory
public class EventToFactory {

    public ActivityInfoTo create(Event event, ResultRestrictionsTo restrictions) {
        return new ActivityInfoTo(
                UUID.randomUUID(),
                event.getReferenceId(),
                event.getChallengeId(),
                event.getName(),
                event.getStages().stream().map(Stage::getName).toList(),
                event.getVehicleClass(),
                event.getCountry(),
                event.getLastResultCheckedTime(),
                event.getEndTime(),
                restrictions);
    }

}
