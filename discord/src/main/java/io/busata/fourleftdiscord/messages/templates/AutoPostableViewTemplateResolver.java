package io.busata.fourleftdiscord.messages.templates;

import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.common.Platform;
import io.busata.fourleft.api.models.VehicleEntryTo;
import io.busata.fourleft.api.models.views.ActivityInfoTo;
import io.busata.fourleft.api.models.views.VehicleTo;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostResultList;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostableView;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.messages.BadgeMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AutoPostableViewTemplateResolver implements TemplateResolver<AutoPostableView> {
    private final DR2FieldMapper fieldMapper;

    @Override
    public String resolve(MessageTemplate template, AutoPostableView value) {
        return StringSubstitutor.replace(template.getTemplate(), buildValuesMap(template, value)).trim();
    }

    public Map<String, String> buildValuesMap(MessageTemplate template, AutoPostableView value) {
        Map<String, String> valueMap = new HashMap<>();

        final var eventInfos = value.getMultiListResults().stream().map(AutoPostResultList::activityInfoTo).collect(Collectors.toList()).get(0);

        String country = eventInfos.stream().map(ActivityInfoTo::country).distinct().map(fieldMapper::createEmoticon).collect(Collectors.joining(" "));
        String vehicleClass = eventInfos.stream().map(ActivityInfoTo::vehicleClass).distinct().map(fieldMapper::createHumanReadable).collect(Collectors.joining(" "));

        String stageName = eventInfos.stream().flatMap((ActivityInfoTo eventInfoTo) -> eventInfoTo.stageNames().stream()).distinct().sorted(Comparator.reverseOrder()).limit(1).collect(Collectors.joining(" • "));

        valueMap.put("countryEmoticon", country);
        valueMap.put("stageNames", stageName);
        valueMap.put("vehicleClassName", vehicleClass);
        valueMap.put("singleList", "");
        valueMap.put("multiList", "");
        valueMap.put("totalEntries", "");

        if (value.getMultiListResults().size() == 1) {
            final var multiListResult = value.getMultiListResults().get(0);
            final var entriesTemplate = template.getRecurringTemplate("entries");
            String entries = multiListResult.results().stream().map(resultEntryTo -> {
                return buildEntry(multiListResult, resultEntryTo, entriesTemplate);
            }).collect(Collectors.joining("\n"));

            valueMap.put("totalEntries", " • *%s entries*".formatted(multiListResult.totalUniqueEntries()));
            valueMap.put("singleList", entries);


        } else {
            String multiList = value.getMultiListResults().stream()
                    .filter(list -> list.results().size() > 0)
                    .map(list -> buildMultiList(template, list))
                    .collect(Collectors.joining("\n"));
            valueMap.put("multiList", multiList);

            int totalEntries = value.getMultiListResults().stream().mapToInt(AutoPostResultList::totalUniqueEntries).sum();
            valueMap.put("totalEntries", " • *%s entries*".formatted(totalEntries));

        }

        return valueMap;
    }

    private String buildMultiList(MessageTemplate template, AutoPostResultList list) {
        final var entriesTemplate = template.getRecurringTemplate("entries");
        final var groupedListTemplate = template.getRecurringTemplate("multiList");

        final var listEntries = list.results().stream().map(resultEntryTo -> {
            return buildEntry(list, resultEntryTo, entriesTemplate);
        }).collect(Collectors.joining("\n"));

        Map<String, String> groupedListValueMap = new HashMap<>();
        groupedListValueMap.put("entries", listEntries);

        groupedListValueMap.put("vehicleRestrictions", "");


        String vehicleRestrictions = list.activityInfoTo().stream().flatMap(activityInfoTo -> activityInfoTo.restrictions().getRestrictedVehicles().stream())
                .map(VehicleTo::displayName).distinct().collect(Collectors.joining(" • "));

        if (StringUtils.isNotBlank(vehicleRestrictions)) {
            groupedListValueMap.put("vehicleRestrictions", "*(%s)*".formatted(vehicleRestrictions));
        }

        groupedListValueMap.put("tierName", list.name());

        return StringSubstitutor.replace(groupedListTemplate, groupedListValueMap);
    }

    public String buildEntry(AutoPostResultList listData, DriverEntryTo entry, String template) {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("rank", String.valueOf(entry.activityRank()));
        valueMap.put("badgeRank", BadgeMapper.createRankBasedIcon(entry.activityRank(), entry.isDnf()));
        valueMap.put("nationalityEmoticon", fieldMapper.createEmoticon(entry.nationality()));
        valueMap.put("name", entry.racenet());
        valueMap.put("totalTime", entry.activityTotalTime());
        valueMap.put("totalDiff", entry.activityTotalDiff());
        valueMap.put("vehicleName", entry.vehicles().get(0).vehicleName());
        valueMap.put("powerStageBadge", determinePowerstageBadge(entry));
        valueMap.put("tierName", listData.name());

        valueMap.put("platform", "");
        valueMap.put("controllerType", "");

        if (entry.platform().platform() != Platform.UNKNOWN) {
            valueMap.put("platform", " %s •".formatted(
                    fieldMapper.createEmoticon(entry.platform().platform().name())
            ));
        }

        if (entry.platform().controller() != ControllerType.UNKNOWN) {
            valueMap.put("controllerType", " %s •".formatted(
                    fieldMapper.createEmoticon(entry.platform().controller().name())
            ));
        }

        final var validVehicle = entry.result().vehicles().stream().allMatch(VehicleEntryTo::vehicleAllowed);

        valueMap.put("validVehicle", validVehicle ? "" : ":pineapple:");

        return StringSubstitutor.replace(template, valueMap);
    }

    private String determinePowerstageBadge(DriverEntryTo entry) {
        if (entry.powerStageRank() > 5) {
            return "";
        }

        return ":rocket: (*%s*) • ".formatted(entry.powerStageRank());

    }
}
