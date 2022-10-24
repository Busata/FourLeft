package io.busata.fourleftdiscord.messages.creation;


import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.api.models.ChannelConfigurationTo;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationMessageFactory {

    public EmbedCreateSpec create(ChannelConfigurationTo configuration) {
        final var builder = EmbedCreateSpec.builder();

        builder.title(configuration.description() + " configuration");
        if(configuration.clubId() != null) {
            builder.addField("Club",
                    "https://dirtrally2.dirtgame.com/clubs/club/%s".formatted(configuration.clubId()), false);
        }

        builder.addField("Post club results?", String.valueOf(configuration.postClubResults()), true);
        builder.addField("Post community events?", String.valueOf(configuration.postCommunityResults()), true);

        return builder.build();
    }
}
