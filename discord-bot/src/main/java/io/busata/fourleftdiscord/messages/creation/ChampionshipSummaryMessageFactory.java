package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.ChampionshipEventEntryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleft.api.models.ChampionshipEventSummaryTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChampionshipSummaryMessageFactory {
    private final DR2FieldMapper fieldMapper;

    public EmbedCreateSpec create(ChampionshipEventSummaryTo summary) {
        final var builder = EmbedCreateSpec.builder();

        builder.title("**%s**".formatted(summary.name()));
        builder.color(Color.of(75, 0, 244));

        builder.addField("Events", "%s".formatted(createEntries(summary.events())), false);

        return builder.build();
    }

    public String createEntries(List<ChampionshipEventEntryTo> events) {
       return events.stream().map(entry -> {
            String message =  String.format("%s • **%s** • %s • %s",
                    fieldMapper.createEmoticon(entry.countryId()),
                    entry.stageName(),
                    fieldMapper.createEmoticon(entry.stageCondition()),
                    fieldMapper.createHumanReadable(entry.vehicleClass())
                    );
            return message;
        }).collect(Collectors.joining("\n"));
    }
}
