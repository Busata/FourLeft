package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class ClubExportConfiguration {

    @GeneratedValue
    @Id
    Long id;

    String clubId;

    @Setter
    boolean enabled;

    public ClubExportConfiguration(String clubId) {
        this.clubId = clubId;
        this.enabled = true;
    }

}
