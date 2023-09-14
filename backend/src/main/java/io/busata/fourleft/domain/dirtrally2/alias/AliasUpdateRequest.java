package io.busata.fourleft.domain.dirtrally2.alias;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
public class AliasUpdateRequest {

    @Id
    @GeneratedValue
    private UUID id;

    private String discordId;

    private String requestedAlias;

    private LocalDateTime requestedUpdateTime;


    public AliasUpdateRequest(String discordId, String requestedAlias) {
        this.discordId = discordId;
        this.requestedAlias = requestedAlias;
        this.requestedUpdateTime = LocalDateTime.now();
    }
}
