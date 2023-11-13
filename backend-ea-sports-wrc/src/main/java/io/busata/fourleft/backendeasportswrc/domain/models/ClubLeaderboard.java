package io.busata.fourleft.backendeasportswrc.domain.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ClubLeaderboard {

    @Id
    String id;

    @Setter
    int totalEntries;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "leaderboard_id")
    List<ClubLeaderboardEntry> entries = new ArrayList<>();


    public ClubLeaderboard(String id, int totalEntries) {
        this.id = id;
        this.totalEntries = totalEntries;
    }

    public void updateEntries(List<ClubLeaderboardEntry> entries) {
        this.entries.clear();
        this.entries.addAll(entries);

    }
}
