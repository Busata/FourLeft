package io.busata.fourleft.domain.discord.bot.models;

import io.busata.fourleft.common.ViewType;
import io.busata.fourleft.common.MessageType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name="message_log")
@Getter
@Setter
public class MessageLog {

    @Id
    @GeneratedValue
    UUID id;

    @Enumerated(EnumType.STRING)
    MessageType messageType;

    @Enumerated(EnumType.STRING)
    ViewType viewType;

    Long messageId;
    Long channelId;
}
