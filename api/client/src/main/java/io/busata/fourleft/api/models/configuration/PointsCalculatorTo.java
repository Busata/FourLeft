package io.busata.fourleft.api.models.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DefaultPointsCalculatorTo.class, name = "defaultPoints"),
        @JsonSubTypes.Type(value = FixedPointsCalculatorTo.class, name = "fixedPoints"),
})
public abstract class PointsCalculatorTo {
}
