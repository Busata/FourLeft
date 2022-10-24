package io.busata.fourleft.domain.discord.repository;

import io.busata.fourleft.domain.discord.models.MessageLog;
import io.busata.fourleft.domain.discord.models.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageLogRepository extends JpaRepository<MessageLog, UUID> {

    boolean existsByMessageIdAndMessageType(Long messagId, MessageType messageType);
}