package io.busata.fourleftdiscord.eawrcsports;


import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.api.easportswrc.EASportsWRCQueueNames;
import io.busata.fourleft.api.easportswrc.events.ChannelUpdatedEvent;
import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageCache {
    private final ConfigurationService configurationService;

    private final EAWRCBackendApi api;

    private final Map<Long, Map<MessageCacheType, EmbedCreateSpec>> cachedData = new HashMap<>();

    private final EmbedFactory embedFactory;


    @PostConstruct
    public void populateCaches() {
        this.updateAll();
    }

    public Optional<EmbedCreateSpec> getMessage(Long channelId, MessageCacheType type) {
        if (this.cachedData.containsKey(channelId)) {
            Map<MessageCacheType, EmbedCreateSpec> messageCacheTypeEmbedCreateSpecMap = this.cachedData.get(channelId);
            if (messageCacheTypeEmbedCreateSpecMap.containsKey(type)) {
                return Optional.ofNullable(messageCacheTypeEmbedCreateSpecMap.get(type));
            }
        }

        return Optional.empty();
    }

    private void updateMessages(Long channelId) {
        log.info("Updating messages for channel {}", channelId);
        this.cachedData.putIfAbsent(channelId, new HashMap<>());

        var currentResults = api.getCurrentResults(channelId);
        var previousResults = api.getPreviousResults(channelId);
        var standings = api.getStandings(channelId);
        var summary = api.getSummary(channelId);


        Map<MessageCacheType, EmbedCreateSpec> messages = this.cachedData.get(channelId);

        currentResults.ifPresent(results -> {
            messages.put(MessageCacheType.RESULTS_CURRENT, embedFactory.create(results));
        });
        previousResults.ifPresent(results -> {
            messages.put(MessageCacheType.RESULTS_PREVIOUS, embedFactory.create(results));
        });
        standings.ifPresent(results -> {
            messages.put(MessageCacheType.RESULTS_STANDINGS, embedFactory.create(results));
        });
        summary.ifPresent(results -> {
            messages.put(MessageCacheType.EVENTS_SUMMARY, embedFactory.create(results));
        });
    }


    @RabbitListener(queues = EASportsWRCQueueNames.EA_SPORTS_WRC_CHANNEL_UPDATE)
    public void updateCache(ChannelUpdatedEvent event) {
        this.updateMessages(event.channelId());
    }

    @RabbitListener(queues = EASportsWRCQueueNames.EA_SPORTS_WRC_READY)
    public void updateAll() {
        this.configurationService.getConfigurations().stream().map(DiscordClubConfigurationTo::channelId).distinct().forEach(this::updateMessages);
    }
}
