package io.busata.fourleftdiscord.messages;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.domain.discord.models.MessageType;
import io.busata.fourleft.domain.discord.models.ViewType;
import io.busata.fourleftdiscord.client.FourLeftClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "io.busata.fourleft.discord", name="enabled", havingValue="true", matchIfMissing = true)
public class DiscordMessageGatewayDefault implements DiscordMessageGateway {
    private final GatewayDiscordClient client;
    private final FourLeftClient api;

    @Override
    public Snowflake postMessage(Snowflake channelId, String entryMessage, MessageType messageType) {
       final var message = client.getChannelById(channelId)
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage(entryMessage)).block();
       logMessage(message, messageType);
       return message.getId();
    }

    @Override
    public void removeMessage(Snowflake channelId, Snowflake messageId) {

        client.getChannelById(channelId).ofType(MessageChannel.class).flatMap(channel -> {
            return channel.getMessageById(messageId).flatMap(Message::delete);
        }).block();
    }

    @Override
    public void editMessage(Snowflake channelId, Snowflake messageId, String content) {
        client.getChannelById(channelId).ofType(MessageChannel.class).flatMap(channel -> {
            return channel.getMessageById(messageId).flatMap(message -> {
                return message.edit(MessageEditSpec.builder().contentOrNull(content).build());
            });
        }).block();
    }
    @Override
    public Message updateMessageEmbeds(Snowflake channelId, Snowflake messageId, MessageEditSpec messageEditSpec)
    {
        return client.getChannelById(channelId).ofType(MessageChannel.class).flatMap(channel -> {
            return channel.getMessageById(messageId).flatMap(message -> {
                return message.edit(messageEditSpec);
            });
        }).block();
    }

    @Override
    public void postMessage(Snowflake channelId, EmbedCreateSpec embed, MessageType messageType) {
        final var message = client.getChannelById(channelId)
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage(embed)).block();

        logMessage(message, messageType);
    }

    @Override
    public void postMessage(Snowflake channelId, List<EmbedCreateSpec> embeds, MessageType messageType) {
        embeds.forEach(embed -> {
            final var message = client.getChannelById(channelId)
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage(embed)).block();

            logMessage(message, messageType);
        });
    }

    @Override
    public void logMessage(Message message, MessageType messageType, ViewType viewType) {
        api.postMessage(new MessageLogTo(
                messageType,
                viewType,
                message.getId().asLong(),
                message.getChannelId().asLong())
        );
    }

    @Override
    public void logMessage(Message message, MessageType messageType) {
        this.logMessage(message, messageType, ViewType.STANDARD);
    }

    @Override
    public Optional<Message> getLastMessage(Snowflake channelId) {
        try {
            return Optional.ofNullable(client.getChannelById(channelId).ofType(MessageChannel.class).flatMap(MessageChannel::getLastMessage).block());
        } catch (Exception ex) {
            log.error("Something went wrong while getting last message");
            return Optional.empty();
        }
    }

    @Override
    public MessageLogTo getMessageDetails(Snowflake messageId) {
        return api.getMessageDetails(messageId.asLong());
    }
}
