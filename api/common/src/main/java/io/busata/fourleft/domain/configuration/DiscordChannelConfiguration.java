package io.busata.fourleft.domain.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

    @Setter
    String description;

    @ManyToOne(cascade = javax.persistence.CascadeType.ALL)
    @JoinColumn(name = "club_view_configuration_id")
    ClubView commandsClubView;

    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    @JoinTable(name = "discord_channel_club_view_configurations",
            joinColumns = {@JoinColumn(name = "discord_channel_configuration_id")},
            inverseJoinColumns = {@JoinColumn(name = "commands_club_view_configuration_id")})
    List<ClubView> commandsClubViews;

    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    @JoinTable(name = "discord_channel_autopost_configurations",
            joinColumns = {@JoinColumn(name = "discord_channel_configuration_id")},
            inverseJoinColumns = {@JoinColumn(name = "club_view_configuration_id")})
    List<ClubView> autopostClubViews;
}
