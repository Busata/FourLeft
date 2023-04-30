package io.busata.fourleftdiscord.commands.results.options;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import io.busata.fourleft.common.ViewType;
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

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@Order(0)
@RequiredArgsConstructor
public class CurrentResultsCommand implements BotCommandOptionHandler {
    private static final String CYCLE_RESULTS_BUTTON_ID = "cycle-current-results";
    private final ResultsFetcher resultsFetcher;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;
    private final DiscordMessageGateway discordMessageGateway;
    private final GatewayDiscordClient discordClient;

    @Override
    public String getCommand() {
        return CommandNames.RESULTS;
    }

    @Override
    public String getOption() {
        return CommandOptions.CURRENT;
    }

    @PostConstruct
    public void setupCommand() {
        discordClient.on(SelectMenuInteractionEvent.class, menuEvent -> {
            if (menuEvent.getCustomId().equals("current-results")) {
                String id = menuEvent.getValues().get(0);
                List<EmbedCreateSpec> results = resultsFetcher.getCurrentEventResultsByViewId(UUID.fromString(id), ViewType.STANDARD);

                Snowflake channelId = menuEvent.getInteraction().getChannelId();
                discordMessageGateway.postMessage(channelId, results, MessageType.RESULTS_POST);
                discordMessageGateway.removeMessage(channelId, menuEvent.getInteraction().getMessageId().orElseThrow());
                return Mono.empty();
            } else {
                return Mono.empty();
            }
        }).subscribe();

        discordClient.on(ButtonInteractionEvent.class, buttonEvent -> {
            return buttonEvent.deferEdit().then(cycleResultView(buttonEvent)).then();
        }).subscribe();
    }

    private Mono<Void> cycleResultView(ButtonInteractionEvent buttonEvent) {
        return Mono.just(buttonEvent).flatMap(event -> {
            if (buttonEvent.getCustomId().equals(CYCLE_RESULTS_BUTTON_ID)) {
                final var messageId = buttonEvent.getMessageId();
                final var channelId = buttonEvent.getInteraction().getChannelId();

                final var messageDetails = discordMessageGateway.getMessageDetails(messageId);

                final var nextViewType = messageDetails.viewType().next();

                List<EmbedCreateSpec> results = resultsFetcher.getCurrentEventResultsByChannelId(channelId, nextViewType);

                MessageEditSpec messageEditSpec = MessageEditSpec.builder()
                        .embeds(results)
                        .addComponent(ActionRow.of(
                                Button.secondary(CYCLE_RESULTS_BUTTON_ID, nextViewType.next().getButtonLabel())
                        ))
                        .build();

                Message message = discordMessageGateway.updateMessageEmbeds(channelId, messageId, messageEditSpec);
                discordMessageGateway.logMessage(message, MessageType.CURRENT_RESULTS_POST, nextViewType);
            }
            return Mono.empty();
        });
    }

    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getOption())
                .description("Get results of the active event")
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
            discordMessageGateway.logMessage(message, MessageType.CURRENT_RESULTS_POST);
        }).then();
    }

    public Mono<Message> createResults(ChatInputInteractionEvent event) {
        try {
            return Mono.just(event).flatMap(evt -> {
                Snowflake channelId = event.getInteraction().getChannelId();
                Button cycleView = Button.secondary(CYCLE_RESULTS_BUTTON_ID, ViewType.STANDARD.next().getButtonLabel());

                List<EmbedCreateSpec> results = resultsFetcher.getCurrentEventResultsByChannelId(channelId, ViewType.STANDARD);

                return event.createFollowup(InteractionFollowupCreateSpec.builder().addAllEmbeds(results).addComponent(ActionRow.of(cycleView)).build());
            });
        } catch (Exception ex) {
            log.error("Error while loading the results", ex);
            return event.createFollowup("*Something went wrong or no active event found? Please try again later!*");
        }
    }
}
