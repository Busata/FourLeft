package io.busata.fourleft.endpoints.views.results;

import io.busata.fourleft.api.models.views.EventInfoTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import io.busata.fourleft.helpers.Factory;

@Factory
public class EventToFactory {

    public EventInfoTo create(Event event) {
        return new EventInfoTo(event.getReferenceId(),
                event.getChallengeId(),
                event.getName(),
                event.getStages().stream().map(Stage::getName).toList(),
                event.getVehicleClass(),
                event.getCountry(),
                event.getLastResultCheckedTime(),
                event.getEndTime());
    }

}
