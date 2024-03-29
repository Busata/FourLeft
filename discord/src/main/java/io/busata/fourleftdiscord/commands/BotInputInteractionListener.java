package io.busata.fourleftdiscord.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import io.busata.fourleft.api.models.discord.DiscordGuildMemberEventTo;
import io.busata.fourleft.common.MemberEvent;
import io.busata.fourleft.common.MessageType;
import io.busata.fourleftdiscord.client.FourLeftClient;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotInputInteractionListener implements EventListener<ChatInputInteractionEvent> {
    private final GatewayDiscordClient client;

    private final List<CommandProvider> commandProviders;

    private final List<BotCommandOptionHandler> commandHandlers;

    private final DiscordMessageGateway discordMessageGateway;

    private final FourLeftClient fourLeftClient;

    @PostConstruct
    public void createCommand() {
        client.on(ButtonInteractionEvent.class, event -> {
            if (event.getCustomId().equals("remove")) {
                return event.getInteraction().getMessageId().map(messageId -> {

                    Snowflake channelId = event.getInteraction().getChannelId();
                    Message messageById = client.getMessageById(channelId, messageId).block();

                    User messageAuthor = messageById.getInteraction().get().getUser();

                    User buttonUser = event.getInteraction().getUser();

                    log.info("Original message user: {}, Button user: {} (ids: {}, {})", messageAuthor.getUsername(), buttonUser.getUsername(), messageAuthor.getId(), buttonUser.getId());

                    if (messageAuthor.getId().equals(buttonUser.getId())) {
                        discordMessageGateway.removeMessage(channelId, messageId);
                        return Mono.empty();
                    } else {
                        return event.reply("Only the original poster can remove this.").withEphemeral(true).then();
                    }
                }).orElseThrow();
            } else {
                return Mono.empty();
            }
        }).subscribe();

        client.on(MemberJoinEvent.class, event -> {
            return Mono.just(event).flatMap(MemberJoinEvent::getGuild).map(guild -> {
                fourLeftClient.notifyMemberEvent(guild.getId().toString(), new DiscordGuildMemberEventTo(
                        event.getMember().getId().toString(),
                        event.getMember().getDisplayName(),
                        MemberEvent.JOINED
                ));

                return Mono.empty();
            });
        }).subscribe();

        client.on(MemberLeaveEvent.class, event -> {
            return Mono.just(event).flatMap(MemberLeaveEvent::getGuild).map(guild -> {
                event.getMember().ifPresent(member -> {
                    fourLeftClient.notifyMemberEvent(guild.getId().toString(), new DiscordGuildMemberEventTo(
                            member.getId().toString(),
                            member.getDisplayName(),
                            MemberEvent.LEFT
                    ));
                });

                return Mono.empty();
            });
        }).subscribe();


        long applicationId = client.getRestClient().getApplicationId().block();


        //deleteExistingCommands(applicationId);
        ImmutableApplicationCommandRequest.Builder rootCommand = ApplicationCommandRequest.builder()
                .name("dr2")
                .description("All commands related to EA Sports WRC");

        commandProviders.forEach(provider -> provider.modify(rootCommand));

//        List.of(DiscordGuilds.BUSATA_DISCORD).forEach(guild -> {
//            commands.forEach(commandRequest -> {
//        commands.forEach(commandRequest -> {
            client.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(applicationId, rootCommand.build())
                    .subscribe();
//        });
//        });
    }

    private void deleteExistingCommands(long applicationId) {
//        List.of(DiscordGuilds.DIRTY_DISCORD, DiscordGuilds.BUSATA_DISCORD, DiscordGuilds.GRF_DISCORD, DiscordGuilds.SCOTTISH_RALLY_GROUP).forEach(guild -> {
        List<Id> discordCommands = client.getRestClient()
                .getApplicationService()
                .getGlobalApplicationCommands(applicationId)
                .map(ApplicationCommandData::id)
                .collectList().block();

        discordCommands.forEach(commandId -> {
            client.getRestClient().getApplicationService().deleteGlobalApplicationCommand(applicationId, commandId.asLong()).subscribe();
        });
    }

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        List<Mono<Void>> results = commandHandlers.stream().map(handler -> Mono.just(event)
                .filter(handler::canHandle)
                .flatMap(evt ->
                        evt.getInteraction()
                                .getChannel()
                                .flatMap(channel -> {
                                    if (handler.canRespond(channel.getId())) {
                                        return handler.handle(evt, channel);
                                    } else {
                                        return event.reply("Sorry, I'm not listening to this command in this channel.").withEphemeral(true).then();
                                    }
                                }).onErrorResume(error -> {
                                    LOG.error("Error handling interaction " + getEventType().getSimpleName(), error);
                                    return Mono.empty();
                                }))).collect(Collectors.toList());

        return Flux.fromIterable(results).flatMap(x -> x).collectList().then();
    }
}
