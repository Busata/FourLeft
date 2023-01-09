package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.helpers.ListHelpers;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ChampionshipStandingsMessageFactory {
    private final DR2FieldMapper fieldMapper;

    public EmbedCreateSpec create(ViewPointsTo result) {
        final var builder = EmbedCreateSpec.builder();
        builder.title("**Championship standings**");
        builder.color(Color.of(244, 0, 75));

        result.getPoints().forEach(singlePointListTo -> {
            final var totalScoringEntries = singlePointListTo.points().stream().filter(entry -> entry.points() >= 1).count();
            final var sortedEntries = singlePointListTo.points().stream()
                    .sorted(Comparator.comparing(PointPairTo::points).reversed())
                    .filter(entry -> entry.points() > 0)
                    .limit(50)
                    .collect(Collectors.toList());

            List<List<PointPairTo>> pointPairGroups = ListHelpers.partitionInGroups(sortedEntries, 12, 8, 10);

            IntStream.range(0, pointPairGroups.size()).forEach(groupIdx -> {
                final var group = pointPairGroups.get(groupIdx);

                final var formattedEntries = IntStream.range(0, group.size()).mapToObj(entryIdx -> {
                    final var entry = group.get(entryIdx);
                    return String.format("**%s** • %s • **%s** • %s",
                            sortedEntries.indexOf(entry) + 1,
                            fieldMapper.createEmoticon(entry.nationality()),
                            entry.name(),
                            entry.points()
                    );
                }).collect(Collectors.joining("\n"));

                final var header = determineStandingsHeader(groupIdx, singlePointListTo);

                builder.addField(header, formattedEntries, false);
            });
            builder.addField("**Total entries**", "*%s*".formatted(totalScoringEntries), false);
        });

        return builder.build();
    }

    private String determineStandingsHeader(int i, SinglePointListTo singlePointListTo) {
        if (i == 0) {
            return StringUtils.isBlank(singlePointListTo.name()) ? "Entries" : singlePointListTo.name();
        } else {
            return "\u200B";
        }
    }
}
