package io.busata.fourleftdiscord.eawrcsports.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import io.busata.fourleft.api.easportswrc.models.SetupChannelResultTo;
import io.busata.fourleftdiscord.eawrcsports.EAWRCBackendApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetupFinderCommandHandler extends ListenerAdapter {
    private final JDA client;
    private final EAWRCBackendApi api;

    @PostConstruct
    public void setupListener() {
        client.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("wrc")) {
            return;
        }
        if (event.getSubcommandGroup() != null) {
            return;
        }
        if (!"setup".equals(event.getSubcommandName())) {
            return;
        }

        String country = event.getOption("country", OptionMapping::getAsString);
        String car = event.getOption("car", OptionMapping::getAsString);

        event.deferReply(true).queue();
        findAndReplySetups(event, country, car);
    }

    private void findAndReplySetups(SlashCommandInteractionEvent event, String country, String car) {
        try {
            List<SetupChannelResultTo> channels = api.getChannels().stream().filter(channel -> channel.name().toLowerCase().contains(country.toLowerCase()) && channel.name().toLowerCase().contains(car.toLowerCase())).toList();
            if (channels.isEmpty()) {
                event.getHook().sendMessage("Could not find any setups.").setEphemeral(true).queue();
            } else {
                String channelResult = channels.stream().map(result -> {
                    return "<#%s>".formatted(result.id());
                }).collect(Collectors.joining("\n"));
                event.getHook().sendMessage("Found the following setups:\n%s".formatted(channelResult)).setEphemeral(true).queue();
            }
        } catch (Exception ex) {
            event.getHook().sendMessage("Something went wrong! Please poke @busata").queue();
        }
    }
}
