package io.busata.fourleft.backendeasportswrc.application.discord.autoposting;


import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.autoposting.AutopostEntry;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordMessageTo;
import io.busata.fourleft.backendeasportswrc.domain.events.AutoPostEditMessageEvent;
import io.busata.fourleft.backendeasportswrc.domain.events.AutoPostNewMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordAutoPostingService {

    public static final int ENTRIES_LIMIT = 10;

    private final DiscordGateway discordGateway;

    private final AutopostingEntryService autopostingEntryService;

    private final ClubService clubService;

    private final ClubLeaderboardService clubLeaderboardService;

    private final DiscordClubConfigurationService clubConfigurationService;

    private final ApplicationEventPublisher publisher;

    public void syncResults(String clubId) {
        List<DiscordClubConfiguration> configurations = clubConfigurationService.findByClubId(clubId);
        configurations.stream().filter(DiscordClubConfiguration::isAutopostingEnabled).forEach(config -> {
            checkAutoposting(clubId, config);
        });
        configurations.stream().filter(DiscordClubConfiguration::isAutopostingDisabled).forEach(config -> {
            saveEntries(clubId, config);
        });
    }

    private void saveEntries(String clubId, DiscordClubConfiguration configuration) {
        this.clubService.getActiveEvent(clubId).ifPresent(event -> {
            String leaderboardId = event.getLeaderboardId();
            List<ClubLeaderboardEntry> entries = clubLeaderboardService.findEntries(leaderboardId);

            List<AutopostEntry> list = entries.stream().map(entry -> {
                return new AutopostEntry(event.getId(), configuration.getChannelId(), -1L, entry.getPlayerKey());
            }).toList();

            autopostingEntryService.saveEntries(list);
        });

    }

    private void checkAutoposting(String clubId, DiscordClubConfiguration configuration) {

        this.clubService.getActiveEvent(clubId).ifPresent(event -> {
            String leaderboardId = event.getLeaderboardId();

            List<ClubLeaderboardEntry> entries = clubLeaderboardService.findEntries(leaderboardId);
            List<AutopostEntry> postedEntries = autopostingEntryService.findPostedEntries(event.getId(), configuration.getChannelId());

            if(entries.size() == postedEntries.size()) {
                return;
            } else {
                syncPosts(configuration, event, entries, postedEntries);
            }
        });
    }

    private void syncPosts(DiscordClubConfiguration configuration, Event event, List<ClubLeaderboardEntry> newEntries, List<AutopostEntry> postedEntries) {

        tryReusingMessage(configuration, postedEntries).ifPresentOrElse(message -> {
            editMessage(configuration, message.id(), event, newEntries, postedEntries);
        }, () -> {
            createNewMessage(configuration, event, newEntries, postedEntries);
        });

    }

    private List<ClubLeaderboardEntry> findToBePosted(Long messageId, List<ClubLeaderboardEntry> newEntries, List<AutopostEntry> postedEntries, boolean requiresTracking) {
        Set<String> postedIds = postedEntries.stream().map(AutopostEntry::getPlayerKey).collect(Collectors.toSet());

        List<ClubLeaderboardEntry> unpostedEntries   = newEntries.stream().filter(entry -> !requiresTracking || entry.isTracked()).filter(newEntry -> !postedIds.contains(newEntry.getPlayerKey())).toList();

        List<AutopostEntry> postedLastTime = postedEntries.stream().filter(postedEntry -> postedEntry.getMessageId().equals(messageId)).toList();
        List<ClubLeaderboardEntry> toBeRepostedEntries = postedLastTime.stream().map(postedEntry -> {
           return  newEntries.stream().filter(newEntry -> newEntry.getPlayerKey().equals(postedEntry.getPlayerKey())).findFirst().orElseThrow();

        }).collect(Collectors.toList());


        int repostLimit = ENTRIES_LIMIT - toBeRepostedEntries.size();

       return Stream.concat(unpostedEntries.stream().sorted(Comparator.comparing(ClubLeaderboardEntry::getRank)).limit(repostLimit), toBeRepostedEntries.stream())
                .sorted(Comparator.comparing(ClubLeaderboardEntry::getRank))
                .toList();
    }


    private Optional<DiscordMessageTo> tryReusingMessage(DiscordClubConfiguration configuration, List<AutopostEntry> postedEntries) {
        return discordGateway.getLastChannelMessage(configuration.getChannelId()).filter(message -> {
            long postedEntriesCount = postedEntries.stream().filter(entry -> entry.getMessageId().equals(message.id())).count();
            return postedEntriesCount > 0 && postedEntriesCount < ENTRIES_LIMIT;
        });
    }

    private void createNewMessage(DiscordClubConfiguration configuration, Event event, List<ClubLeaderboardEntry> newEntries, List<AutopostEntry> postedEntries) {
        List<String> postedIds = postedEntries.stream().map(AutopostEntry::getPlayerKey).collect(Collectors.toList());
        List<ClubLeaderboardEntry> toBePosted   = newEntries.stream().filter(entry -> !configuration.isRequiresTracking() || entry.isTracked()).filter(newEntry -> !postedIds.contains(newEntry.getPlayerKey())).limit(ENTRIES_LIMIT).sorted(Comparator.comparing(ClubLeaderboardEntry::getRank)).toList();

        publisher.publishEvent(new AutoPostNewMessageEvent(configuration.getChannelId(), new AutoPostMessageSummary(event, newEntries.size(), toBePosted)));
    }

    private void editMessage(DiscordClubConfiguration configuration, Long messageId, Event event, List<ClubLeaderboardEntry> newEntries, List<AutopostEntry> postedEntries) {
        List<ClubLeaderboardEntry> toBePosted = findToBePosted(messageId, newEntries, postedEntries, configuration.isRequiresTracking());
        publisher.publishEvent(new AutoPostEditMessageEvent(configuration.getChannelId(), messageId, new AutoPostMessageSummary(event,newEntries.size(), toBePosted)));

    }


}
