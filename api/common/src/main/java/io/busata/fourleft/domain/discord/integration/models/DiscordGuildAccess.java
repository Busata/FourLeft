package io.busata.fourleft.domain.discord.integration.models;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class DiscordGuildAccess {

    @GeneratedValue
    @Id
    UUID id;

    String discordUserId;

    @ElementCollection
    List<String> guildIds;

    LocalDateTime lastInviteSentTime;

    /*
        The server admin gets to see a list of users that are part of the server.
        He can add people from that list to the "allowed to manage list"

        He can also invite them to the application. Which should sent them a discord message
        with a link to the registration page.

        Keep track of the date someone has been sent the message as not to spam them.


        When loading the guilds for a user, we can check if the users id is in the access list and show it/allow access.
        

     */
}
