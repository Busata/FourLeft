package io.busata.fourleft.api.models.configuration;


import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @Type(value = SingleClubViewTo.class, name = "singleClub"),
        @Type(value = TiersViewTo.class, name = "tiersView"),
        @Type(value = CommunityChallengeViewTo.class, name = "communityChallengeView")
})
public abstract class ResultsViewTo {
    public abstract boolean includesClub(long clubId);
}
