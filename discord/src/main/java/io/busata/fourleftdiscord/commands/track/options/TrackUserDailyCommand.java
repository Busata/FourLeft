package io.busata.fourleftdiscord.commands.track.options;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import io.busata.fourleftdiscord.client.FourLeftClient;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandOptions;
import io.busata.fourleft.api.models.ChannelConfigurationTo;
import io.busata.fourleft.api.models.TrackUserRequestTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class
TrackUserDailyCommand implements BotCommandOptionHandler {
    private final FourLeftClient api;

    @Override
    public String getCommand() {
        return CommandNames.TRACK;
    }
    @Override
    public String getOption() {
        return CommandOptions.RESULTS;
    }
    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getOption())
                .description("Track user for the daily weekly monthly")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("racenet")
                        .description("Your racenet username (Check if it works on https://dr2.today)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    @Override
    public List<Snowflake> getResponseChannels() {
        return List.of(Snowflake.of(892369709780070410L));
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return Mono.just(event).flatMap(evt -> {
            return evt.deferReply().withEphemeral(true).then(trackUser(event));
        });
    }

    private Mono<Void> trackUser(ChatInputInteractionEvent event) {
            String racenet = event.getOption(getOption())
                    .flatMap(subCommand -> subCommand.getOption("racenet"))
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

            List<String> closestMatches = api.queryUsername(racenet);

            api.trackUser(new TrackUserRequestTo(racenet));

            return event.createFollowup(
                    "Tracking racenet user **%s**. Manage any aliases with /results alias <racenet>. \n*Closest Racenet matches were:\n%s\n. Having issues? Contact @Busata*".formatted(
                            racenet,
                            String.join("\n", closestMatches)
                    )
            ).withEphemeral(true).then();
    }
}
