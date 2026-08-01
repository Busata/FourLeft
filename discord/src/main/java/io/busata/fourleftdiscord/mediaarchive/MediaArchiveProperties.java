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

    /** Discord user ids allowed to run the backfill command without being a server admin. */
    private List<Long> operatorUserIds = new ArrayList<>();

    public boolean isArchivedChannel(long channelId) {
        return channelIds.contains(channelId);
    }

    public boolean isOperator(long userId) {
        return operatorUserIds.contains(userId);
    }
}
