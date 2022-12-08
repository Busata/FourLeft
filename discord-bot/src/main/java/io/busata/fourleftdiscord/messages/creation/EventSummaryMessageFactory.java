package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.ChampionshipEventEntryTo;
import io.busata.fourleft.api.models.views.ViewEventEntryTo;
import io.busata.fourleft.api.models.views.ViewEventSummaryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleft.api.models.ChampionshipEventSummaryTo;
import io.busata.fourleftdiscord.helpers.ListHelpers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventSummaryMessageFactory {
    private final DR2FieldMapper fieldMapper;

    public EmbedCreateSpec create(ViewEventSummaryTo summary) {
        final var builder = EmbedCreateSpec.builder();

        builder.title("**%s**".formatted(summary.header()));
        builder.color(Color.of(75, 0, 244));

        List<List<ViewEventEntryTo>> partitionInGroups = ListHelpers.partitionInGroups(summary.events(), 4);
        for (int i = 0; i < partitionInGroups.size(); i++) {
            List<ViewEventEntryTo> events = partitionInGroups.get(i);
            builder.addField(i == 0 ? "Events" : "\u200B", "%s".formatted(createEntries(events)), false);
        }

        return builder.build();
    }

    public String createEntries(List<ViewEventEntryTo> events) {
       return events.stream().map(entry ->
               String.format("**%s** • **%s**\n%s %s",
                       fieldMapper.createEmoticon(entry.countryId()),
                       fieldMapper.createHumanReadable(entry.vehicleClass()),
                       String.join(", ", entry.stageNames()),
                       entry.stageNames().size() == 1 ? "• " + fieldMapper.createEmoticon(entry.stageCondition()) : ""
                )
       ).collect(Collectors.joining("\n"));
    }
}
