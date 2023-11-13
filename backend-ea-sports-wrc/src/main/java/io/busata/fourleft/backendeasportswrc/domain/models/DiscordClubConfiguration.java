package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
public class DiscordClubConfiguration {

    @GeneratedValue
    @Id
    UUID id;

    String clubId;

    Long channelId;

    boolean enabled;

    boolean requiresTracking;

    boolean autopostingEnabled;


    public boolean isAutopostingDisabled() {
        return !autopostingEnabled;
    }


}
