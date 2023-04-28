package io.busata.fourleftdiscord.messages.creation;

import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostableView;
import io.busata.fourleftdiscord.messages.templates.AutoPostableViewTemplateResolver;
import io.busata.fourleftdiscord.messages.templates.MessageTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import static io.busata.fourleftdiscord.messages.templates.MessageTemplate.messageTemplate;

@Component
@RequiredArgsConstructor
public class AutoPostResultMessageFactory {
    private final AutoPostableViewTemplateResolver templateResolver;

    MessageTemplate BASE = messageTemplate("""
            **Results** • **${countryEmoticon}** • **${stageNames}** • **${vehicleClassName}**
            ${singleList}${multiList}""")
            .withRecurringTemplate(
                    "multiList",
                    """
                            **${tierName}** ${vehicleRestrictions}
                            ${entries}
                            """
            );
    MessageTemplate NORMAL_TEMPLATE = BASE.copy()
            .withRecurringTemplate(
                    "entries",
                    "**${rank}** • **${nationalityEmoticon}** • **${name}** •${platform}${controllerType} ${totalTime} *(${totalDiff})* • *${vehicleName}*");

    MessageTemplate POWERSTAGE_TEMPLATE = BASE.copy()
            .withRecurringTemplate(
                    "entries",
                    "${powerStageBadge}**${rank}** • **${nationalityEmoticon}** • **${name}** •${platform}${controllerType} ${totalTime} *(${totalDiff})* • *${vehicleName}*");

    MessageTemplate RANKED_BADGE_TEMPLATE = BASE.copy()
            .withRecurringTemplate(
                    "entries",
                    "*${badgeRank}* **${rank}** • **${nationalityEmoticon}** • **${name}** •${platform}${controllerType} ${totalTime} *(${totalDiff})* • *${vehicleName}*");

    MessageTemplate POWERSTAGE_AND_RANKED_BADGE_TEMPLATE = BASE.copy()
            .withRecurringTemplate(
                    "entries",
                    "${powerStageBadge}*${badgeRank}* **${rank}** • **${nationalityEmoticon}** • **${name}** •${platform}${controllerType} ${totalTime} *(${totalDiff})* • *${vehicleName}*");


    public String createAutopostMessage(AutoPostableView view) {

        final var template = determineTemplate(view);

        return templateResolver.resolve(template, view);
    }

    private MessageTemplate determineTemplate(AutoPostableView view) {
        return createResultListTemplate(view.getViewProperties());
    }

    private MessageTemplate createResultListTemplate(ViewPropertiesTo properties) {
        if (properties.powerStage()) {
            return switch (properties.badgeType()) {
                case NONE -> POWERSTAGE_TEMPLATE;
                case PERCENTAGE -> NORMAL_TEMPLATE;
                case RANKED -> POWERSTAGE_AND_RANKED_BADGE_TEMPLATE;
            };
        } else {
            return switch (properties.badgeType()) {
                case NONE -> NORMAL_TEMPLATE;
                case PERCENTAGE -> NORMAL_TEMPLATE;
                case RANKED -> RANKED_BADGE_TEMPLATE;
            };
        }
    }

}
