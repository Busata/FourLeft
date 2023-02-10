package io.busata.fourleftdiscord.messages.templates;

import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleftdiscord.messages.BadgeMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ResultEntryTemplateResolver implements TemplateResolver<ResultEntryTo> {
    private final DR2FieldMapper fieldMapper;

    @Override
    public String resolve(MessageTemplate template, ResultEntryTo value) {
        return StringSubstitutor.replace(template.getTemplate(), buildValuesMap(value));
    }

    public Map<String, String> buildValuesMap(ResultEntryTo entry) {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("rank",String.valueOf(entry.rank()));
        valueMap.put("badgeRank", BadgeMapper.createRankBasedIcon(entry.rank(), entry.isDnf()));
        valueMap.put("nationalityEmoticon",fieldMapper.createEmoticon(entry.nationality()));
        valueMap.put("vehicle",entry.vehicle());
        valueMap.put("name",entry.name());
        valueMap.put("totalTime",entry.totalTime());
        valueMap.put("totalDiff",entry.totalDiff());
        valueMap.put("platform", fieldMapper.createEmoticon(entry.platform().name()));
        valueMap.put("controllerType", fieldMapper.createEmoticon(entry.controllerType().name()));

        return valueMap;
    }
}
