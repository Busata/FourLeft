package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.tiers.VehicleTo;
import io.busata.fourleft.api.models.views.EventInfoTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.api.models.views.SingleResultListTo;
import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.helpers.ListHelpers;
import io.busata.fourleftdiscord.messages.templates.MessageTemplate;
import io.busata.fourleftdiscord.messages.templates.ResultEntryTemplateResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static io.busata.fourleftdiscord.messages.templates.MessageTemplate.messageTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClubEventResultMessageFactory {
    private final DR2FieldMapper fieldMapper;
    private final ResultEntryTemplateResolver templateResolver;

    MessageTemplate NORMAL_TEMPLATE = messageTemplate(
                    "**${rank}** • **${nationalityEmoticon}** • **${name}** • ${platform} • ${totalTime} *(${totalDiff})*");

    MessageTemplate RANKED_BADGE_TEMPLATE =  messageTemplate(
                    "*${badgeRank}* **${rank}** • **${nationalityEmoticon}** • **${name}** • ${platform} • ${totalTime} *(${totalDiff})*");

    public List<EmbedCreateSpec> createDefault(ViewResultTo clubResultTo) {
        final var template = getTemplate(clubResultTo.getViewPropertiesTo());
        return create(clubResultTo, template, false);
    }

    public List<EmbedCreateSpec> createPowerStage(ViewResultTo clubResultTo) {
        final var template = getTemplate(clubResultTo.getViewPropertiesTo());
        return create(clubResultTo, template, true);
    }

    private MessageTemplate getTemplate(ViewPropertiesTo properties) {
        return switch (properties.badgeType()) {
            case NONE -> NORMAL_TEMPLATE;
            case PERCENTAGE -> throw new UnsupportedOperationException();
            case RANKED -> RANKED_BADGE_TEMPLATE;
        };
    }

    protected List<EmbedCreateSpec> create(ViewResultTo clubResult, MessageTemplate entryTemplate, boolean powerstageOnly) {
        List<EmbedCreateSpec> specs = new ArrayList<>();

        var builder = EmbedCreateSpec.builder();

        final var eventInfos = clubResult.getMultiListResults().stream().map(SingleResultListTo::eventInfoTo).collect(Collectors.toList());

        String country = eventInfos.stream().map(EventInfoTo::country).distinct().map(fieldMapper::createEmoticon).collect(Collectors.joining(" "));
        String vehicleClass = eventInfos.stream().map(EventInfoTo::vehicleClass).distinct().map(fieldMapper::createHumanReadable).collect(Collectors.joining(" "));

        List<String> stageNames = eventInfos.stream().findFirst().stream().flatMap(eventInfoTo -> eventInfoTo.stageNames().stream()).collect(Collectors.toList());

        builder.title("**Results**");
        builder.color(Color.of(244, 0, 75));

        String countryImage = eventInfos.stream().map(EventInfoTo::country).findFirst().map(fieldMapper::createImage).orElse("");

        builder.addField("Country", "%s".formatted(country), true);
        builder.addField("Car", vehicleClass, true);
        builder.addField( stageNames.size() > 0 ? "Stages" : "Stage", String.join(", ", stageNames), true);
        builder.footer(EmbedCreateFields.Footer.of("\u2800".repeat(40), null));

        if(clubResult.getMultiListResults().size() > 1) {
            specs.add(builder.build());
            builder = createFullWidthBuilder();
        }


        List<SingleResultListTo> multiListResults = clubResult.getMultiListResults();
        for (int i = 0; i < multiListResults.size(); i++) {
            SingleResultListTo singleResultList = multiListResults.get(i);

            if(singleResultList.results().size() == 0 && multiListResults.size() > 1) {
                builder.description("*No entries yet*");
            }

            String vehicleRestrictions = "\u200B";
            if (singleResultList.restrictions() instanceof ResultListRestrictionsTo restrictions) {
                vehicleRestrictions = "*(%s)*".formatted(restrictions.getAllowedVehicles().stream().map(VehicleTo::displayName).collect(Collectors.joining(" • ")));
            }

            if (StringUtils.isNotBlank(singleResultList.name())) {
                builder.addField("**%s**".formatted(singleResultList.name()), "%s".formatted(vehicleRestrictions), false);
            }

            if (clubResult.getViewPropertiesTo().powerStage()) {
                addPowerStageField(singleResultList, builder);
            }

            if (!powerstageOnly) {
                final var sortedEntries = singleResultList.results().stream().sorted(Comparator.comparing(ResultEntryTo::rank)).limit(50).collect(Collectors.toList());


                int groupSize = 10;
                var groupedEntries = ListHelpers.partitionInGroups(sortedEntries, groupSize);

                //Temporary fix to avoid field sizes being too big
                boolean reduceEntries = groupedEntries.stream().map(ge -> ge.stream().map(entry -> templateResolver.resolve(entryTemplate, entry)).collect(Collectors.joining("\n"))).map(String::length).anyMatch(entrySize -> entrySize >= 1024);

                if(reduceEntries) {
                    groupSize = 5;
                    groupedEntries = ListHelpers.partitionInGroups(sortedEntries, groupSize);
                }

                int bound = groupedEntries.size();
                for (int groupIdx = 0; groupIdx < bound; groupIdx++) {
                    final var group = groupedEntries.get(groupIdx);
                    String collect = group.stream().map(entry -> templateResolver.resolve(entryTemplate, entry)).collect(Collectors.joining("\n"));
                    builder.addField(determineHeader(groupIdx, groupSize), collect, false);
                }
            }



            if(i == multiListResults.size() - 1) {
                builder.addField("**Last update**", "*%s*".formatted(new PrettyTime().format(singleResultList.eventInfoTo().lastUpdate())), true);
                if(singleResultList.totalEntries() > 0) {
                    builder.addField("**Total entries**", "*%s*".formatted(singleResultList.totalEntries()), true);
                }
                builder.addField("**Event ending**", "<t:%s:R>".formatted(singleResultList.eventInfoTo().endTime().toInstant().atZone(ZoneOffset.UTC).toEpochSecond()), true);
            }

            specs.add(builder.build());
            builder = createFullWidthBuilder();
        }

        return specs;
    }

    private static EmbedCreateSpec.Builder createFullWidthBuilder() {
        return EmbedCreateSpec.builder()
                .color(Color.of(new Random().nextFloat(),new Random().nextFloat(),new Random().nextFloat()));
    }

    private void addPowerStageField(SingleResultListTo clubResult, EmbedCreateSpec.Builder builder) {
        final var powerstageEntries = clubResult.results().stream().sorted(Comparator.comparing(ResultEntryTo::stageTime)).limit(5).collect(Collectors.toList());

        if(powerstageEntries.size() > 0) {
            StringJoiner joiner = new StringJoiner("\n");
            for (int i = 0; i < powerstageEntries.size(); i++) {
                ResultEntryTo powerstageEntry = powerstageEntries.get(i);
                String format = String.format("%s **%s** • **%s** • **%s** • %s *(%s)*",
                        ":rocket:",
                        i + 1,
                        fieldMapper.createEmoticon(powerstageEntry.nationality()),
                        powerstageEntry.name(),
                        powerstageEntry.stageTime(),
                        powerstageEntry.stageDiff()
                );
                joiner.add(format);
            }
            builder.addField("Powerstage *(%s)*".formatted(clubResult.eventInfoTo().stageNames().get(clubResult.eventInfoTo().stageNames().size() - 1)), joiner.toString(), false);
        }
    }
    private String determineHeader(int idx, int groupSize) {
        if (idx == 0) {
            return "Top %s".formatted(groupSize);
        } else {
            var startBound = (idx * groupSize) + 1;
            var endBound = (idx * groupSize) + groupSize;

            return "Top %s - %s".formatted(startBound, endBound);
        }
    }

}
