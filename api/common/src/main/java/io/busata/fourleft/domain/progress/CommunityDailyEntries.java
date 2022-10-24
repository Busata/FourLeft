package io.busata.fourleft.domain.progress;


import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;

@Table(name="community_daily_entries")
@Entity
@Getter
public class CommunityDailyEntries {

    @Id
    @GeneratedValue
    UUID id;

    String name;
    String totalTime;
    String totalDiff;

    boolean isDnf;

    Long rank;
    Long totalRank;

    String country;
    ZonedDateTime endTime;
    String stageName;
    String vehicleClass;
    String vehicle;
    UUID leaderboardId;

}
