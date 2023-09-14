package io.busata.fourleft.domain.dirtrally2.alias;

import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;


@Entity
@NoArgsConstructor
public class AliasUpdateLog {
    @Id
    @GeneratedValue
    UUID id;

    String discordId;

    String changes;


    public AliasUpdateLog(String discordId, String changes) {
        this.discordId = discordId;
        this.changes = changes;
    }
}
