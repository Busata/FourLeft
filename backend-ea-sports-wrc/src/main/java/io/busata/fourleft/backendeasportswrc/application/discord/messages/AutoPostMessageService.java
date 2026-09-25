package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.AutopostingEntryService;
import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.events.AutoPostEditMessageEvent;
import io.busata.fourleft.backendeasportswrc.domain.events.AutoPostNewMessageEvent;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.autoposting.AutopostEntry;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordMessageTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutoPostMessageService {
    private final DiscordGateway discordGateway;
    private final AutopostingEntryService autopostingEntryService;
    private final DiscordClubConfigurationService discordClubConfigurationService;

    public static String defaultTemplate = """
            **Results** • ${eventCountryFlag} • **${lastStage}** • **${eventVehicleClass}** • *${totalEntries} entries*
            |entries:**${rank}** • ${flag} • **${displayName}** • ${platform} • ${totalTime} *${deltaTime}* • *${vehicle}*|
            """;

    private final AutoPostTemplateResolver resolver;

    // Discord's message content limit.
    static final int MAX_MESSAGE_LENGTH = 2000;

    @EventListener
    public void handleNewMessage(AutoPostNewMessageEvent event) {
        String template = discordClubConfigurationService.findByChannelId(event.channelId()).map(DiscordClubConfiguration::getAutoPostTemplate).orElse(defaultTemplate);
        AutoPostMessageSummary summary = fit(template, event.summary());
        String message = resolver.render(template, summary);

        if (message.length() >= MAX_MESSAGE_LENGTH) {
            log.warn("Message too large, not posting"); //TODO
        } else {
            if (!summary.entries().isEmpty()) {
                DiscordMessageTo postedMessage = discordGateway.createMessage(event.channelId(), new SimpleDiscordMessageTo(message, List.of()));
                List<AutopostEntry> collect = summary.entries().stream().map(entry -> {

                    return new AutopostEntry(summary.eventIdOf(entry), event.channelId(), postedMessage.id(), entry.getPlayerKey());
                }).collect(Collectors.toList());


                autopostingEntryService.saveEntries(collect);
            }


        }
    }

    @EventListener
    public void editExistingMessage(AutoPostEditMessageEvent event) {
        String template = discordClubConfigurationService.findByChannelId(event.channelId()).map(DiscordClubConfiguration::getAutoPostTemplate).orElse(defaultTemplate);

        AutoPostMessageSummary summary = fit(template, event.summary());
        String message = resolver.render(template, summary);

        if (message.length() >= MAX_MESSAGE_LENGTH) {
            log.warn("Message too large, not posting"); //TODO
        } else {
            if (!summary.entries().isEmpty()) {
                DiscordMessageTo editedMessage = discordGateway.editMessage(event.channelId(), event.messageId(), new SimpleDiscordMessageTo(message, List.of()));

                //ignore the entries already posted(?)
                List<AutopostEntry> collect = summary.entries().stream().map(entry -> {
                    return new AutopostEntry(summary.eventIdOf(entry), event.channelId(), editedMessage.id(), entry.getPlayerKey());
                }).collect(Collectors.toList());


                autopostingEntryService.saveEntries(collect);
            }

        }

    }

    /**
     * Drops the lowest-ranked entries until the message fits Discord's limit — class tags make mixed-channel
     * entries longer. Dropped entries aren't recorded as posted, so the next sync posts them.
     */
    AutoPostMessageSummary fit(String template, AutoPostMessageSummary summary) {
        List<ClubLeaderboardEntry> entries = new ArrayList<>(summary.entries());
        AutoPostMessageSummary fitted = summary;
        while (entries.size() > 1 && resolver.render(template, fitted).length() >= MAX_MESSAGE_LENGTH) {
            entries.remove(lowestRanked(summary, entries));
            fitted = summary.withEntries(List.copyOf(entries));
        }
        return fitted;
    }

    private static int lowestRanked(AutoPostMessageSummary summary, List<ClubLeaderboardEntry> entries) {
        int lowest = 0;
        for (int i = 1; i < entries.size(); i++) {
            if (rankOf(summary, entries.get(i)) > rankOf(summary, entries.get(lowest))) {
                lowest = i;
            }
        }
        return lowest;
    }

    private static long rankOf(AutoPostMessageSummary summary, ClubLeaderboardEntry entry) {
        AutoPostMessageSummary.MixedEntry mixed = summary.mixed().get(entry);
        return mixed != null ? mixed.rank() : entry.getRankAccumulated();
    }
}
