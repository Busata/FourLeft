package io.busata.fourleft.api.models.views;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NoResultRestrictionsTo.class, name = "NO_RESTRICTIONS"),
        @JsonSubTypes.Type(value = ResultListRestrictionsTo.class, name = "RESULTS_LIST_RESTRCTIONS"),
})
public abstract class ResultRestrictionsTo {

}
