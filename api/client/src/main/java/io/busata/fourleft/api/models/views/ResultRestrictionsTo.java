package io.busata.fourleft.api.models.views;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.busata.fourleft.api.models.messages.MessageCreateEvent;
import io.busata.fourleft.api.models.messages.MessageDeleteEvent;
import io.busata.fourleft.api.models.messages.MessageUpdateEvent;
import io.busata.fourleft.api.models.tiers.VehicleTo;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NoResultRestrictionsTo.class, name = "NO_RESTRICTIONS"),
        @JsonSubTypes.Type(value = ResultListRestrictionsTo.class, name = "RESULTS_LIST_RESTRCTIONS"),
})
public abstract class ResultRestrictionsTo {

}
