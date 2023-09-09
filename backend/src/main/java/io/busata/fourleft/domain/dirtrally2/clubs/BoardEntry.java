package io.busata.fourleft.domain.dirtrally2.clubs;

import io.busata.fourleft.domain.dirtrally2.players.PlayerInfo;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
@Setter
@Getter

public class BoardEntry {
    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="leaderboard_id")
    private Leaderboard leaderboard;

    private long rank;

    private String name;

    public String getName() {
        return this.playerInfo.getDisplayName();
    }

    @ManyToOne()
    @JoinColumn(name="player_info_id")
    private PlayerInfo playerInfo;

    private String vehicleName;

    //Replace these with durations
    private String stageTime;
    private String stageDiff;
    private String totalTime;
    private String totalDiff;
    private String nationality;

    private boolean isDnf;

    public boolean hasFinished() {
        return !isDnf;
    }

}
