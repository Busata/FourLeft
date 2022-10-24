package io.busata.fourleftdiscord.messages.templates;

import io.busata.fourleft.api.models.overview.ClubResultSummaryTo;
import io.busata.fourleft.api.models.overview.CommunityResultSummaryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.messages.BadgeMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CommunitySummaryResultTemplateResolver implements TemplateResolver<CommunityResultSummaryTo> {
    private final DR2FieldMapper fieldMapper;

    @Override
    public String resolve(MessageTemplate template, CommunityResultSummaryTo value) {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("rank",String.valueOf(value.rank()));
        valueMap.put("totalEntries",String.valueOf(value.totalEntries()));
        valueMap.put("badgeRank", BadgeMapper.createPercentageBasedIcon(value.percentageRank(), value.isDnf()));
        valueMap.put("nationalityEmoticon",fieldMapper.createEmoticon(value.nationality()));
        valueMap.put("vehicle",value.vehicle());
        valueMap.put("vehicleClass",fieldMapper.createHumanReadable(value.vehicleClass()));
        valueMap.put("countryEmoticon", fieldMapper.createEmoticon(value.countryName()));
        valueMap.put("totalTime",value.totalTime());
        valueMap.put("totalDiff",value.totalDiff());
        valueMap.put("endTime", "<t:%s:R>".formatted(value.endTime().toInstant().atZone(ZoneOffset.UTC).toEpochSecond()));


        return StringSubstitutor.replace(template.getTemplate(), valueMap);
    }

}
