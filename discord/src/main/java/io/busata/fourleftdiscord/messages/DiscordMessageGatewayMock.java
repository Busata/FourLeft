package io.busata.fourleftdiscord.messages;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.api.messages.MessageType;
import io.busata.fourleft.api.models.ViewType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "io.busata.fourleft.discord", name="enabled", havingValue="false")
public class DiscordMessageGatewayMock implements DiscordMessageGateway {

    @Override
    public Snowflake postMessage(Snowflake channelId, String entryMessage, MessageType messageType) {
        return channelId;
    }

    @Override
    public void removeMessage(Snowflake channelId, Snowflake messageId) {

    }

    @Override
    public void editMessage(Snowflake channelId, Snowflake messageId, String content) {

    }

    @Override
    public Message updateMessageEmbeds(Snowflake channelId, Snowflake messageId, MessageEditSpec messageEditSpec) {
        return null;
    }

    @Override
    public void postMessage(Snowflake channelId, EmbedCreateSpec embed, MessageType messageType) {

    }

    @Override
    public void postMessage(Snowflake channelId, List<EmbedCreateSpec> embed, MessageType messageType) {

    }

    @Override
    public void logMessage(Message message, MessageType messageType, ViewType type) {

    }

    @Override
    public void logMessage(Message message, MessageType messageType) {

    }

    @Override
    public Optional<Message> getLastMessage(Snowflake channelId) {
        return Optional.empty();
    }

    @Override
    public MessageLogTo getMessageDetails(Snowflake messageId) {
        return null;
    }
}
