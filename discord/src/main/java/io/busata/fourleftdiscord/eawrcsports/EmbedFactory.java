package io.busata.fourleftdiscord.eawrcsports;

import discord4j.core.spec.EmbedCreateSpec;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class EmbedFactory {

    public EmbedCreateSpec create(String embedData) {
        DataObject dataObject = DataObject.fromJson(embedData);
        EmbedBuilder embedBuilder = EmbedBuilder.fromData(dataObject);

        MessageEmbed messageEmbed = embedBuilder.build();
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();

        transferFields(messageEmbed, builder);

        return builder.build();


    }

    private void transferFields(MessageEmbed messageEmbed, EmbedCreateSpec.Builder embedCreateBuilder) {


        optionally(messageEmbed::getDescription).ifPresent(embedCreateBuilder::description);
        optionally(messageEmbed::getTitle).ifPresent(embedCreateBuilder::title);
        messageEmbed.getFields().forEach(field-> {
            embedCreateBuilder.addField(
                    field.getName(),
                    field.getValue(),
                    field.isInline()
            );
        });
    }

    private <T> Optional<T> optionally(Supplier<T> supplier) {
        return Optional.ofNullable(supplier.get());
    }
}
