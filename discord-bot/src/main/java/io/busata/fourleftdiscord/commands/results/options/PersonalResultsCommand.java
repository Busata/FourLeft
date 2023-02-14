package io.busata.fourleftdiscord.commands.results.options;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleft.api.models.configuration.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import io.busata.fourleftdiscord.client.FourLeftClient;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandOptions;
import io.busata.fourleftdiscord.messages.MessageTemplateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PersonalResultsCommand implements BotCommandOptionHandler {
    @Override
    public String getCommand() {
        return CommandNames.RESULTS;
    }
    @Override
    public String getOption() {
        return CommandOptions.PERSONAL;
    }
    private final DiscordChannelConfigurationService discordChannelConfigurationService;

    private final MessageTemplateFactory messageTemplateFactory;
    private final FourLeftClient api;


    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getOption())
                .description("Personal results")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("username")
                        .description("User")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    @Override
    public List<Snowflake> getResponseChannels() {
        return discordChannelConfigurationService.getConfigurations().stream().map(DiscordChannelConfigurationTo::channelId).map(Snowflake::of).toList();
   }

    @Override
    public boolean canRespond(Snowflake channelId) {
        return true;
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return Mono.just(event).flatMap(evt -> {
            return evt.deferReply().then(getPersonalResults(event));
        }).then();
    }

    private Mono<Message> getPersonalResults(ChatInputInteractionEvent event) {
        try {
            String username = event.getOption(getOption())
                    .flatMap(subCommand -> subCommand.getOption("username"))
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

            return Mono.just(event).flatMap((evt) -> {
                boolean useBadges = event.getInteraction().getGuildId().map(Snowflake::asLong).map(id -> id == 892050958723469332L).orElse(false);

                final var result = api.getUserResultSummary(username);
                List<EmbedCreateSpec> embedFromUserResultSummary = List.of(messageTemplateFactory.createEmbedFromUserResultSummary(username, result, useBadges));

                Button removeButton = Button.secondary("remove", ReactionEmoji.unicode("\u1F5D1"));

                InteractionFollowupCreateSpec build = InteractionFollowupCreateSpec.builder()
                        .embeds(embedFromUserResultSummary)
                        .addComponent(ActionRow.of(removeButton))
                        .build();

                return event.createFollowup(build);

            });
        }
        catch (Exception ex) {
            log.error("Something went wrong fetching personal results", ex);
            return event.createFollowup("*Something went wrong!*").withEphemeral(true);
        }
    }
}
