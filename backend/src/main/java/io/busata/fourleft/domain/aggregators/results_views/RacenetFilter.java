package io.busata.fourleft.domain.aggregators.results_views;

import io.busata.fourleft.common.RacenetFilterMode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
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

    boolean enabled;

    public RacenetFilter(String name, RacenetFilterMode filterMode, List<String> racenetNames, boolean enabled) {
        this.name = name;
        this.filterMode = filterMode;
        this.racenetNames = racenetNames;
        this.enabled = enabled;
    }
}
