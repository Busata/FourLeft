package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.backendeasportswrc.application.discord.messages.AutoPostMessageService;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubResultsMessageFactory;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class DiscordClubConfiguration {

    @GeneratedValue
    @Id
    UUID id;

    String clubId;

    Long guildId;
    Long channelId;

    boolean enabled;

    boolean requiresTracking;

    boolean autopostingEnabled;


    String resultsEntryTemplate;
    String autoPostTemplate;


    public boolean isAutopostingDisabled() {
        return !autopostingEnabled;
    }


    public DiscordClubConfiguration(Long guildId, Long channelId, String clubId, boolean autopostingEnabled) {
        this.guildId = guildId;
        this.clubId = clubId;
        this.channelId = channelId;
        this.enabled = true;
        this.autopostingEnabled = autopostingEnabled;
        this.requiresTracking = false;
        this.autoPostTemplate = AutoPostMessageService.defaultTemplate;
        this.resultsEntryTemplate = ClubResultsMessageFactory.defaultTemplate;

    }
}
