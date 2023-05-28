package io.busata.fourleft.domain.discord;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DiscordGuildMember {

    @Id
    String id;
    String userName;
    String guildId;
}
