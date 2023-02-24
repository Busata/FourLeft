package io.busata.fourleft.domain.clubs.repository;

import io.busata.fourleft.domain.clubs.models.DR2CommunityEventType;

import java.time.LocalDate;

public interface CommunityChallengeSummaryTo {
    Long getRank();
    Boolean getIsDnf();
    String getName();
    Long getTotal();
    LocalDate getChallengeDate();
    DR2CommunityEventType getType();
}
