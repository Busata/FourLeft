package io.busata.fourleftdiscord.commands.results.options;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandOptions;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import io.busata.fourleft.common.MessageType;
import io.busata.fourleftdiscord.messages.ResultsFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class PreviousResultsCommand implements BotCommandOptionHandler {
    @Override
    public String getCommand() { return CommandNames.RESULTS; }
    @Override
    public String getOption() { return CommandOptions.PREVIOUS; }
    private final ResultsFetcher resultsFetcher;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;

    private final DiscordMessageGateway discordMessageGateway;


    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getOption())
                .description("Get results of the last event")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .build();
    }

    @Override
    public List<Snowflake> getResponseChannels() {
        return discordChannelConfigurationService.getConfigurations().stream().map(DiscordChannelConfigurationTo::channelId).map(Snowflake::of).toList();
  }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return event.deferReply().then(createResults(event)).doOnNext(message -> {
            discordMessageGateway.logMessage(message, MessageType.RESULTS_POST);
        }).then();
    }

    public Mono<Message> createResults(ChatInputInteractionEvent event) {
        try {
            return Mono.just(event).flatMap(evt -> {
                List<EmbedCreateSpec> results = resultsFetcher.getPreviousEventResults(event.getInteraction().getChannelId());

                return event.createFollowup(InteractionFollowupCreateSpec.builder().addAllEmbeds(results).build());
            });
        } catch (Exception ex) {
            log.error("Error while loading the results", ex);
            return event.createFollowup("*Something went wrong. Please try again later!*");
        }
    }
}
