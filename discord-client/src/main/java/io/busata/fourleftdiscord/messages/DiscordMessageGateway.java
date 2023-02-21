package io.busata.fourleftdiscord.messages;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.domain.discord.models.MessageType;
import io.busata.fourleft.domain.discord.models.ViewType;

import java.util.List;
import java.util.Optional;

public interface DiscordMessageGateway {
    Snowflake postMessage(Snowflake channelId, String entryMessage, MessageType messageType);

    void removeMessage(Snowflake channelId, Snowflake messageId);

    void editMessage(Snowflake channelId, Snowflake messageId, String content);

    Message updateMessageEmbeds(Snowflake channelId, Snowflake messageId, MessageEditSpec messageEditSpec);

    void postMessage(Snowflake channelId, EmbedCreateSpec embed, MessageType messageType);
    void postMessage(Snowflake channelId, List<EmbedCreateSpec> embed, MessageType messageType);

    void logMessage(Message message, MessageType messageType, ViewType type);
    void logMessage(Message message, MessageType messageType);

    Optional<Message> getLastMessage(Snowflake channelId);

    MessageLogTo getMessageDetails(Snowflake messageId);
}
