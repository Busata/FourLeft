package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Stage {

    @Id
    String id;

    String leaderboardId;

    @Embedded
    StageSettings stageSettings;


    public Stage(String id, String leaderboardId, StageSettings stageSettings) {
        this.id = id;
        this.leaderboardId = leaderboardId;
        this.stageSettings = stageSettings;
    }
}
