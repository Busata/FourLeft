package io.busata.fourleft.domain.clubs.repository;

import io.busata.fourleft.domain.clubs.models.DR2CommunityEventType;

public interface CommunityChallengeSummaryTo {
    Long getRank();
    Boolean getIsDnf();
    String getName();
    Long getTotal();
    DR2CommunityEventType getType();
}
