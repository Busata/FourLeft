package io.busata.fourleft.api.models.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageCreateEvent.class, name = "MessageCreateEvent"),
        @JsonSubTypes.Type(value = MessageDeleteEvent.class, name = "MessageDeleteEvent"),
        @JsonSubTypes.Type(value = MessageUpdateEvent.class, name = "MessageUpdateEvent"),
})
public interface MessageEvent {

    UUID getId();
}
