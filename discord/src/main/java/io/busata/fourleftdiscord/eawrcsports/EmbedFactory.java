package io.busata.fourleftdiscord.eawrcsports;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Service;

@Service
public class EmbedFactory {

    public MessageEmbed create(String embedData) {
        return EmbedBuilder.fromData(DataObject.fromJson(embedData)).build();
    }
}
