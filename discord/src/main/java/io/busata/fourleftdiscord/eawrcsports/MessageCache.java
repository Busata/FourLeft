package io.busata.fourleftdiscord.eawrcsports;


import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.api.easportswrc.EASportsWRCQueueNames;
import io.busata.fourleft.api.easportswrc.events.LeaderboardUpdatedEvent;
import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageCache {
    private final ConfigurationService configurationService;

    private final EAWRCBackendApi api;

    private final Map<String, Map<MessageCacheType, EmbedCreateSpec>> cachedData = new HashMap<>();

    private final EmbedFactory embedFactory;


    @PostConstruct
    public void populateCaches() {
        this.updateAll();
    }

    public Optional<EmbedCreateSpec> getMessage(Long channelId, MessageCacheType type) {
        return this.configurationService.getClubId(channelId).flatMap(clubId -> {
            if (this.cachedData.containsKey(clubId)) {
                Map<MessageCacheType, EmbedCreateSpec> messageCacheTypeEmbedCreateSpecMap = this.cachedData.get(clubId);
                if (messageCacheTypeEmbedCreateSpecMap.containsKey(type)) {
                    return Optional.ofNullable(messageCacheTypeEmbedCreateSpecMap.get(type));
                }
            }

            return Optional.empty();
        });
    }

    private void updateMessages(String clubId) {
        this.cachedData.putIfAbsent(clubId, new HashMap<>());

        var currentResults = api.getCurrentResults(clubId);
        var previousResults = api.getPreviousResults(clubId);
        var standings = api.getStandings(clubId);
        var summary = api.getSummary(clubId);


        Map<MessageCacheType, EmbedCreateSpec> messages = this.cachedData.get(clubId);

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


    @RabbitListener(queues = EASportsWRCQueueNames.EA_SPORTS_WRC_LEADERBOARD_UPDATE)
    public void updateCache(LeaderboardUpdatedEvent event) {
        this.updateMessages(event.clubId());
    }

    @RabbitListener(queues = EASportsWRCQueueNames.EA_SPORTS_WRC_READY)
    public void updateAll() {
        this.configurationService.getConfigurations().stream().map(DiscordClubConfigurationTo::clubId).distinct().forEach(this::updateMessages);
    }
}
