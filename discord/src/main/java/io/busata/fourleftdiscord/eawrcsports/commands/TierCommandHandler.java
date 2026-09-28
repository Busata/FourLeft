package io.busata.fourleftdiscord.eawrcsports.commands;

import feign.FeignException;
import io.busata.fourleft.api.easportswrc.models.TierSetCreateTo;
import io.busata.fourleft.api.easportswrc.models.TierSetLinkRequestTo;
import io.busata.fourleft.api.easportswrc.models.TierSetLinkTo;
import io.busata.fourleftdiscord.eawrcsports.EAWRCBackendApi;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * /fourleft tiers create|edit: both reply with a private link to the tier set's edit page, where tiers and
 * players are managed. Sets are scoped to the guild they were created in.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TierCommandHandler extends ListenerAdapter {
    private static final String LINK = "https://fourleft.io/easportswrc/tiers/%s";
    // Discord's cap on autocomplete choices.
    private static final int MAX_CHOICES = 25;

    private final JDA client;
    private final EAWRCBackendApi api;

    @PostConstruct
    public void setupListener() {
        client.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("fourleft") || !"tiers".equals(event.getSubcommandGroup())) {
            return;
        }
        if (event.getGuild() == null) {
            event.reply("Tier sets can only be managed from a server.").setEphemeral(true).queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();
        String discordId = event.getUser().getId();
        String name = event.getOption("name", OptionMapping::getAsString);

        try {
            if ("create".equals(event.getSubcommandName())) {
                TierSetLinkTo link = api.createTierSet(new TierSetCreateTo(name, guildId, discordId));
                event.reply("Tier set **%s** created. Add tiers and players [here](%s).".formatted(link.name(), LINK.formatted(link.linkId()))).setEphemeral(true).queue();
            } else if ("edit".equals(event.getSubcommandName())) {
                TierSetLinkTo link = api.requestTierSetLink(new TierSetLinkRequestTo(name, guildId, discordId));
                event.reply("Manage tier set **%s** [here](%s).".formatted(link.name(), LINK.formatted(link.linkId()))).setEphemeral(true).queue();
            }
        } catch (FeignException.Conflict e) {
            event.reply("A tier set named **%s** already exists here; use `/fourleft tiers edit` to manage it.".formatted(name)).setEphemeral(true).queue();
        } catch (FeignException.NotFound e) {
            event.reply("No tier set named **%s** in this server.".formatted(name)).setEphemeral(true).queue();
        } catch (FeignException.BadRequest e) {
            event.reply("The name must be 1-100 characters.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("fourleft") || !"tiers".equals(event.getSubcommandGroup())
                || !"name".equals(event.getFocusedOption().getName()) || event.getGuild() == null) {
            return;
        }

        String typed = event.getFocusedOption().getValue().toLowerCase();
        List<String> names = api.getTierSetNames(event.getGuild().getIdLong()).stream()
                .filter(name -> name.toLowerCase().contains(typed))
                .limit(MAX_CHOICES)
                .toList();
        event.replyChoiceStrings(names).queue();
    }
}
