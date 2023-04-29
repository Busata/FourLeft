package io.busata.fourleft.domain.dirtrally2.clubs.models;

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
@Getter
@Setter
public class BoardEntry {
    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="leaderboard_id")
    private Leaderboard leaderboard;

    private long rank;
    private String name;
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
