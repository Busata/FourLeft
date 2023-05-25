package io.busata.fourleft.domain.discord.integration.models;

import lombok.Getter;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
public class UserDiscordGuildAccess {

    @Id
    UUID userId;

    @ElementCollection
    List<String> guildIds;
}
