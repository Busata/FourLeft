package io.busata.fourleftdiscord.mediaarchive;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "discord.media-archive")
@Getter
@Setter
public class MediaArchiveProperties {
    /** Channels whose image attachments get archived to the media gallery. */
    private List<Long> channelIds = new ArrayList<>();

    public boolean isArchivedChannel(long channelId) {
        return channelIds.contains(channelId);
    }
}
