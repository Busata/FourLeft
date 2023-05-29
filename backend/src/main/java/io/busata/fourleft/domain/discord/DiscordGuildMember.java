package io.busata.fourleft.domain.discord;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DiscordGuildMember {

    @Id
    @GeneratedValue
    UUID id;

    String discordId;
    String userName;

    String guildId;
}
