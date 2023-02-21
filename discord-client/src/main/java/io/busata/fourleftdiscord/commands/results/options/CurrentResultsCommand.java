package io.busata.fourleftdiscord.commands.results.options;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleft.api.models.configuration.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandOptions;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import io.busata.fourleft.domain.discord.bot.models.MessageType;
import io.busata.fourleftdiscord.messages.ResultsFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@Order(0)
@RequiredArgsConstructor
public class CurrentResultsCommand implements BotCommandOptionHandler {
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
            if(menuEvent.getCustomId().equals("current-results")) {
                String id = menuEvent.getValues().get(0);
                List<EmbedCreateSpec> results = resultsFetcher.getCurrentEventResults(UUID.fromString(id));

                Snowflake channelId = menuEvent.getInteraction().getChannelId();
                discordMessageGateway.postMessage(channelId,results, MessageType.RESULTS_POST);
                discordMessageGateway.removeMessage(channelId, menuEvent.getInteraction().getMessageId().orElseThrow());
                return Mono.empty();
            } else {
                return Mono.empty();
            }
        }).subscribe();
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
            discordMessageGateway.logMessage(message, MessageType.RESULTS_POST);
        }).then();
    }

    public Mono<Message> createResults(ChatInputInteractionEvent event) {
        try {
            return Mono.just(event).flatMap(evt -> {
                Snowflake channelId = event.getInteraction().getChannelId();

                final var channelConfiguration = discordChannelConfigurationService.findConfigurationByChannelId(channelId);
                if (channelConfiguration.commandClubViews().size() > 1) {

                    SelectMenu menu = SelectMenu.of("current-results", channelConfiguration.commandClubViews().stream().map(clubView -> {
                                return SelectMenu.Option.of(clubView.description(), clubView.id().toString());
                            }).collect(Collectors.toList())
                    );

                    Button removeButton = Button.danger("remove", "Remove");


                    return event.createFollowup(InteractionFollowupCreateSpec.builder()
                            .ephemeral(true)
                            .content("Which view?")
                            .addComponent(ActionRow.of(menu))
                            .addComponent(ActionRow.of(removeButton))
                            .build());
                }


                List<EmbedCreateSpec> results = resultsFetcher.getCurrentEventResults(channelId);

                return event.createFollowup(InteractionFollowupCreateSpec.builder().addAllEmbeds(results).build());
            });
        } catch (Exception ex) {
            log.error("Error while loading the results", ex);
            return event.createFollowup("*Something went wrong or no active event found? Please try again later!*");
        }
    }
}
