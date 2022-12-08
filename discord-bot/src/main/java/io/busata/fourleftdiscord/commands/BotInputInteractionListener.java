package io.busata.fourleftdiscord.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotInputInteractionListener implements EventListener<ChatInputInteractionEvent> {
    private final GatewayDiscordClient client;

    private final List<CommandProvider> commandProviders;

    private final List<BotCommandOptionHandler> commandHandlers;

    @PostConstruct
    public void createCommand() {
        long applicationId = client.getRestClient().getApplicationId().block();



        //deleteExistingCommands(applicationId);

        List<ImmutableApplicationCommandRequest> commands = commandProviders.stream().map(CommandProvider::create).collect(Collectors.toList());

//        List.of(DiscordGuilds.BUSATA_DISCORD).forEach(guild -> {
          //  commands.forEach(commandRequest -> {
            //    client.getRestClient().getApplicationService()
              //          .createGlobalApplicationCommand(applicationId, commandRequest)
                //        .subscribe();
            //});
//        });
    }

    private void deleteExistingCommands(long applicationId) {
//        List.of(DiscordGuilds.DIRTY_DISCORD, DiscordGuilds.BUSATA_DISCORD, DiscordGuilds.GRF_DISCORD, DiscordGuilds.SCOTTISH_RALLY_GROUP).forEach(guild -> {
            List<String> discordCommands = client.getRestClient()
                    .getApplicationService()
                    .getGlobalApplicationCommands(applicationId)
                    .map(ApplicationCommandData::id)
                    .collectList().block();

            discordCommands.forEach(commandId -> {
                client.getRestClient().getApplicationService().deleteGlobalApplicationCommand(applicationId, Long.parseLong(commandId)).subscribe();
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
                                    if(handler.canRespond(channel.getId())) {
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
