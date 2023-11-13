package io.busata.fourleft.backendeasportswrc.domain.models.profile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
public class ProfileUpdateRequest {

    @Id
    @GeneratedValue
    private UUID id;

    private String discordId;

    private String requestedSSID;

    private LocalDateTime requestedUpdateTime;


    public ProfileUpdateRequest(String discordId, String requestedSSID) {
        this.discordId = discordId;
        this.requestedSSID = requestedSSID;
        this.requestedUpdateTime = LocalDateTime.now();
    }
}
