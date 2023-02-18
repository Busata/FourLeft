package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.CommunityChallengeBoardEntryTo;
import io.busata.fourleft.api.models.CommunityChallengeSummaryTo;
import io.busata.fourleft.domain.clubs.models.DR2CommunityEventType;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.messages.BadgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CommunityEventMessageFactory {
    private final DR2FieldMapper fieldMapper;

    private List<Color> embedColours = List.of(
            Color.of(244, 0, 75),
            Color.ORANGE,
            Color.JAZZBERRY_JAM,
            Color.GREEN,
            Color.RED,
            Color.MAGENTA,
            Color.TAHITI_GOLD,
            Color.DISCORD_WHITE,
            Color.BROWN,
            Color.CYAN,
            Color.ENDEAVOUR,
            Color.WHITE,
            Color.SUBMARINE
    );

    public List<EmbedCreateSpec> getEmbeds(List<CommunityChallengeSummaryTo> communityResults) {

        List<EmbedCreateSpec.Builder> dailies = communityResults
                .stream()
                .filter(this::isDaily)
                .filter(this::hasEntries)
                .map(this::createEmbed).collect(Collectors.toList());

        List<EmbedCreateSpec.Builder> weeklies = communityResults
                .stream()
                .filter(this::isWeekly)
                .filter(this::hasEntries)
                .map(this::createEmbed).collect(Collectors.toList());

        List<EmbedCreateSpec.Builder> monthlies = communityResults
                .stream()
                .filter(this::isMonthly)
                .filter(this::hasEntries)
                .map(this::createEmbed).collect(Collectors.toList());

        final var infoMessage = List.of(EmbedCreateSpec.builder().footer("Add your name to the board? /track results", null));

        List<EmbedCreateSpec.Builder> collect = Stream.of(dailies, weeklies, monthlies, infoMessage)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        for (int i = 0; i < collect.size(); i++) {
            collect.get(i).color(embedColours.get(i));
        }

        return collect.stream().map(EmbedCreateSpec.Builder::build).collect(Collectors.toList());
    }

    private boolean isMonthly(CommunityChallengeSummaryTo communityChallengeSummaryTo) {
        return communityChallengeSummaryTo.type().equals(DR2CommunityEventType.Monthly);
    }

    private boolean hasEntries(CommunityChallengeSummaryTo communityChallengeSummaryTo) {
        return communityChallengeSummaryTo.entries().size() > 0;
    }

    private boolean isDaily(CommunityChallengeSummaryTo communityChallengeSummaryTo) {
        return communityChallengeSummaryTo.type().equals(DR2CommunityEventType.Daily);
    }

    private boolean isWeekly(CommunityChallengeSummaryTo communityChallengeSummaryTo) {
        return communityChallengeSummaryTo.type().equals(DR2CommunityEventType.Weekly);
    }
    private EmbedCreateSpec.Builder createEmbed(CommunityChallengeSummaryTo communityChallenge) {
        final var builder = EmbedCreateSpec.builder();
        CommunityChallengeBoardEntryTo firstEntry = communityChallenge.firstEntry();
        CommunityChallengeBoardEntryTo topOnePercentEntry = communityChallenge.topOnePercentEntry();

        builder.title("**%s Challenge** • %s • **%s** • **%s**".formatted(
                communityChallenge.type().name(),
                communityChallenge.eventLocations().stream().map(fieldMapper::createEmoticon).collect(Collectors.joining(" ")),
                communityChallenge.firstStageName(),
                fieldMapper.createHumanReadable(communityChallenge.vehicleClass())
        ));

        builder.description("**Fastest driver**:\n :first_place: • %s • **%s** • *%s*\n%s\n<:blank:894976571406966814>".formatted(
                fieldMapper.createEmoticon(firstEntry.nationality()),
                firstEntry.name(),
                firstEntry.stageTime(),
                "**Top 1%% Target Time:**\n<:Rank_S:971454722030600214> **%s** • **%s** • *(%s)*"
                        .formatted(
                                topOnePercentEntry.stageTime(),
                                ordinal((int) topOnePercentEntry.rank()),
                                topOnePercentEntry.stageDiff()
                        )
        ));

        final var sortedEntries = communityChallenge.entries();

        final var fieldsRequired = (int) Math.ceil(sortedEntries.size() / 10f);


        for (int i = 0; i < fieldsRequired; i++) {
            String collect = sortedEntries.stream().skip(i * 10L).limit(10).map(boardEntry -> {
                return String.format("%s • %s • **%s** • **%s** • *(%s)*",
                        createBadge(boardEntry),
                        fieldMapper.createEmoticon(boardEntry.nationality()),
                        boardEntry.name(),
                        ordinal((int) boardEntry.rank()),
                        boardEntry.stageDiff()
                );
            }).collect(Collectors.joining("\n"));

            builder.addField(determineHeader(i), collect, false);
        }

        builder.footer("Number of entries (global): %s".formatted(firstEntry.totalRank()), null);

        return builder;
    }

    private String determineHeader(int i) {
        if (i == 0) {
            return "DiRTy Gossip Drivers:";
        } else {
            return "\u200B";
        }
    }

    public String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        return switch (i % 100) {
            case 11, 12, 13 -> i + "th";
            default -> i + suffixes[i % 10];
        };
    }

    private String createBadge(CommunityChallengeBoardEntryTo entry) {
        final var badge = BadgeMapper.createPercentageBasedIcon(entry.percentageRank(), entry.isDnf());
        return String.format("%s **Top %s%%**", badge, (int) Math.ceil(entry.percentageRank()));
    }

}
