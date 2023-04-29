package io.busata.fourleft.domain.dirtrally2.clubs.repository;

import io.busata.fourleft.common.DR2CommunityEventType;

import java.time.LocalDate;

public interface CommunityChallengeSummaryProjection {
    Long getRank();
    Boolean getIsDnf();
    String getName();
    Long getTotal();
    LocalDate getChallengeDate();
    DR2CommunityEventType getType();
}
