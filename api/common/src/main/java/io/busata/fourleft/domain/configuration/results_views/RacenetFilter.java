package io.busata.fourleft.domain.configuration.results_views;

import io.busata.fourleft.domain.configuration.player_restrictions.RacenetFilterMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class RacenetFilter {

    @Id
    @GeneratedValue
    UUID id;

    String name;

    @Enumerated(EnumType.STRING)
    RacenetFilterMode filterMode;

    @ElementCollection
    List<String> racenetNames;

    public RacenetFilter(String name, RacenetFilterMode filterMode, List<String> racenetNames) {
        this.name = name;
        this.filterMode = filterMode;
        this.racenetNames = racenetNames;
    }
}
