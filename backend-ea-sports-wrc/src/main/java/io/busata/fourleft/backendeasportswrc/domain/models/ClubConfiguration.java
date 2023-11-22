package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ClubConfiguration {

    @GeneratedValue
    @Id
    Long id;

    String clubId;

    boolean keepSynced;

    public ClubConfiguration(String clubId) {
        this.clubId = clubId;
        this.keepSynced = true;
    }

    //Automate stuff


    public void setKeepSynced(boolean keepSynced) {
        this.keepSynced = keepSynced;
    }
}
