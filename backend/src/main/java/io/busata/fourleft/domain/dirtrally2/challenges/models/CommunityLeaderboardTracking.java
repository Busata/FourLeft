package io.busata.fourleft.domain.dirtrally2.challenges.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Getter
@Setter
public class CommunityLeaderboardTracking {
    @Id
    @GeneratedValue
    UUID id;

    String nickName;
    String alias;

    boolean trackRallyCross;
    boolean trackDaily;
    boolean trackMonthly;
    boolean trackWeekly;
}
