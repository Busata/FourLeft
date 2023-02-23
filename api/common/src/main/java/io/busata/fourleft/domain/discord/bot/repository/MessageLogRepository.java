package io.busata.fourleft.domain.discord.bot.repository;

import io.busata.fourleft.domain.discord.bot.models.MessageLog;
import io.busata.fourleft.domain.discord.bot.models.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageLogRepository extends JpaRepository<MessageLog, UUID> {

    boolean existsByMessageIdAndMessageType(Long messagId, MessageType messageType);

    Optional<MessageLog> findByMessageId(long messageId);
}