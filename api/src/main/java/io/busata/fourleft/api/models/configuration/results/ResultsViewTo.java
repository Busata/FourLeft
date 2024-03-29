package io.busata.fourleft.api.models.configuration.results;


import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @Type(value = SingleClubViewTo.class, name = "singleClub"),
        @Type(value = MergedViewTo.class, name = "mergeClub"),
        @Type(value = PartitionViewTo.class, name = "partitionClub"),
        @Type(value = ConcatenationViewTo.class, name = "concatenationClub"),
        @Type(value = CommunityChallengeViewTo.class, name = "communityChallengeView")
})
public abstract class ResultsViewTo {

    @Setter
    @Getter
    private UUID id;

    @Getter
    @Setter
    private Set<Long> associatedClubs;
}
