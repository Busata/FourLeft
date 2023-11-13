package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.AutopostingEntryService;
import io.busata.fourleft.backendeasportswrc.domain.models.autoposting.AutopostEntry;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordMessageTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import io.busata.fourleft.backendeasportswrc.domain.events.AutoPostEditMessageEvent;
import io.busata.fourleft.backendeasportswrc.domain.events.AutoPostNewMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutoPostMessageService {
    private final DiscordGateway discordGateway;
    private final AutopostingEntryService autopostingEntryService;

    public static String baseTemplate = """
            **Results** • ${eventCountryFlag} • **${lastStage}** • **${eventVehicleClass}** • *${totalEntries} entries*
            |entries:${rankBadge} • **${rank}** • ${flag} • **${displayName}** • ${platform} • ${totalTime} *${deltaTime}* • *${vehicle}*|
            """;

    private final AutoPostTemplateResolver resolver;

    @EventListener
    public void handleNewMessage(AutoPostNewMessageEvent event) {
        String message = resolver.render(baseTemplate, event.summary());

        if(message.length() >= 2000) {
            log.warn("Message too large, not posting"); //TODO
        } else {
            DiscordMessageTo postedMessage = discordGateway.createMessage(event.channelId(), new SimpleDiscordMessageTo(message, List.of()));


            List<AutopostEntry> collect = event.summary().entries().stream().map(entry -> {

                return new AutopostEntry(event.summary().event().getId(), event.channelId(), postedMessage.id(), entry.getPlayerKey());
            }).collect(Collectors.toList());


            autopostingEntryService.saveEntries(collect);
        }
    }

    @EventListener
    public void editExistingMessage(AutoPostEditMessageEvent event) {
        String message = resolver.render(baseTemplate, event.summary());

        if(message.length() >= 2000) {
            log.warn("Message too large, not posting"); //TODO
        } else {
            DiscordMessageTo editedMessage = discordGateway.editMessage(event.channelId(), event.messageId(), new SimpleDiscordMessageTo(message, List.of()));


            //ignore the entries already posted(?)
            List<AutopostEntry> collect = event.summary().entries().stream().map(entry -> {
                return new AutopostEntry(event.summary().event().getId(),event.channelId(),  editedMessage.id(), entry.getPlayerKey());
            }).collect(Collectors.toList());


            autopostingEntryService.saveEntries(collect);
        }

    }
}
