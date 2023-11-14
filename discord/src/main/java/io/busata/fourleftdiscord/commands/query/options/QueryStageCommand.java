package io.busata.fourleftdiscord.commands.query.options;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleftdiscord.client.FourLeftClient;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandOptions;
import io.busata.fourleft.api.models.QueryTrackResultsTo;
import io.busata.fourleftdiscord.messages.creation.QueryMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class QueryStageCommand implements BotCommandOptionHandler {

    private final QueryMessageFactory queryMessageFactory;
    private final FourLeftClient api;

    @Override
    public String getCommand() {
        return CommandNames.QUERY;
    }
    @Override
    public String getOption() {
        return CommandOptions.STAGE;
    }
    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getOption())
                .description("Query stage name")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("stage")
                        .description("Part of a stage name")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return event.deferReply().then(queryStage(event));
    }

    public Mono<Void> queryStage(ChatInputInteractionEvent event) {
        return Mono.just(event).flatMap(evt -> {

            String stageName = event.getOption(getCommand())
                    .flatMap(command -> command.getOption(getOption()))
                    .flatMap(subCommand -> subCommand.getOption("stage"))
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

            try {
                QueryTrackResultsTo queryTrackResultsTo = api.queryTrack(stageName);
                EmbedCreateSpec embedCreateSpec = queryMessageFactory.create(queryTrackResultsTo);

                Button removeButton = Button.danger("remove", "Remove");

                return evt.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(embedCreateSpec).addComponent(ActionRow.of(removeButton)).build()).then();
            } catch (Exception ex) {
                log.info("Something went wrong, input: {}", stageName, ex);
                return evt.createFollowup("Could not find any stage with this query").withEphemeral(true).then();
            }

        });
    }
}
