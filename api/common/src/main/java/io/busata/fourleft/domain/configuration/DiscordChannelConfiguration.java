package io.busata.fourleft.domain.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DiscordChannelConfiguration {

    @Id
    @GeneratedValue
    UUID id;

    Long channelId;

    String description;

    @ManyToOne
    @JoinColumn(name = "club_view_configuration_id")
    ClubView commandsClubView;


    @ManyToMany
    @JoinTable(name = "discord_channel_autopost_configurations",
            joinColumns = {@JoinColumn(name = "discord_channel_configuration_id")},
            inverseJoinColumns = {@JoinColumn(name = "club_view_configuration_id")})
    List<ClubView> autopostClubViews;
}
