package io.busata.fourleft.domain.discord.integration.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDiscordGuildAccess {

    @Id
    String discordId;

    @ElementCollection
    List<String> guildIds;
}
