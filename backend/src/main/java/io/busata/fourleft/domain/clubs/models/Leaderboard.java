package io.busata.fourleft.domain.clubs.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Leaderboard {
    @Id
    @GeneratedValue

    UUID id;
    String challengeId;
    String eventId;
    String stageId;

    @OneToMany(mappedBy = "leaderboard", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BoardEntry> entries = new ArrayList<>();

    public void updateEntries(List<BoardEntry> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
    }
}
