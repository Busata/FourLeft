package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.ClubMemberTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClubMembersMessageFactory {

    public EmbedCreateSpec create(List<ClubMemberTo> members) {
        final var builder = EmbedCreateSpec.builder();
        builder.title("**Club members (%s members)**".formatted(String.valueOf(members.size())));
        builder.color(Color.of(244, 0, 75));

        final var sortedByParticipation = members.stream().sorted(Comparator.comparing(ClubMemberTo::championshipParticipation).reversed()).limit(5).collect(Collectors.toList());
        builder.addField(":tickets: Most entries", "%s".formatted(createEntries(sortedByParticipation, (entry) -> String.valueOf(entry.championshipParticipation()))), false);

        final var sortedByGold = members.stream().sorted(Comparator.comparing(ClubMemberTo::championshipGolds).reversed()).limit(5).collect(Collectors.toList());
        builder.addField(":trophy: • Most 1st Places",  "%s".formatted(createEntries(sortedByGold, (entry) -> String.valueOf(entry.championshipGolds()))), false);

        final var sortedBySilver = members.stream().sorted(Comparator.comparing(ClubMemberTo::championshipSilvers).reversed()).limit(5).collect(Collectors.toList());
        builder.addField(":second_place: • Most 2nd Places", "%s".formatted(createEntries(sortedBySilver, (entry) -> String.valueOf(entry.championshipSilvers()))), false);

        final var sortedByBronze = members.stream().sorted(Comparator.comparing(ClubMemberTo::championshipBronzes).reversed()).limit(5).collect(Collectors.toList());
        builder.addField(":third_place: • Most 3rd Places", "%s".formatted(createEntries(sortedByBronze, (entry) -> String.valueOf(entry.championshipBronzes()))), false);

        return builder.build();
    }

    public String createEntries(List<ClubMemberTo> members, Function<ClubMemberTo, String> entryCount) {
       return members.stream().map(entry -> {
            return String.format("**%s** • %s", entryCount.apply(entry), entry.displayName() );
        }).collect(Collectors.joining("\n"));
    }
}
