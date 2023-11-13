package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class ClubConfiguration {

    @GeneratedValue
    @Id
    Long id;

    String clubId;

    boolean keepSynced;

    //Automate stuff


    public void setKeepSynced(boolean keepSynced) {
        this.keepSynced = keepSynced;
    }
}
