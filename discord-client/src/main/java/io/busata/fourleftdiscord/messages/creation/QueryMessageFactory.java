package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.QueryTrackResultsTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryMessageFactory {
    private final DR2FieldMapper fieldMapper;

    public EmbedCreateSpec create(QueryTrackResultsTo result) {
        final var builder = EmbedCreateSpec.builder();
        builder.color(Color.of(244, 0, 75));

        String countryEmoticon = fieldMapper.createEmoticon(result.countryId());
        builder.title("%s • %s".formatted(countryEmoticon, result.countryName()));


        String stageConfiguration = List.of(
                "**%s (%s km)**".formatted(result.longStage().displayName(), result.longStage().length()),
                "• • %s (%s km)".formatted(result.firstShort().displayName(), result.firstShort().length()),
                "• • %s (%s km)".formatted(result.secondShort().displayName(), result.secondShort().length())
        ).stream().collect(Collectors.joining("\n"));

        String reverseStageConfiguration = List.of(
                "**%s (%s km)**".formatted(result.reverseLongStage().displayName(), result.reverseLongStage().length()),
                "• • %s (%s km)".formatted(result.reverseFirstShort().displayName(), result.reverseFirstShort().length()),
                "• • %s (%s km)".formatted(result.reverseSecondShort().displayName(), result.reverseSecondShort().length())
        ).stream().collect(Collectors.joining("\n"));


        builder.addField(
                "\u200B",
                stageConfiguration,
                false);

        builder.addField(
                "\u200B",
                reverseStageConfiguration,
                false);


        return builder.build();
    }

}
