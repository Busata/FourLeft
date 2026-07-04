package io.busata.fourleft.backendeasportswrc.domain.models.configuration;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
public class ChannelConfigurationRequest {

    @Id
    @GeneratedValue
    private UUID id;

    private Long guildId;

    private Long channelId;

    private String discordId;

    private LocalDateTime requestedTime;

    public ChannelConfigurationRequest(Long guildId, Long channelId, String discordId) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.discordId = discordId;
        this.requestedTime = LocalDateTime.now();
    }
}
