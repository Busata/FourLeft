package io.busata.fourleft.api;

import io.busata.fourleft.api.models.FieldMappingRequestTo;
import io.busata.fourleft.api.models.FieldMappingTo;
import io.busata.fourleft.common.MessageType;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface FourLeftApi {

    @GetMapping(RoutesTo.FIELD_MAPPINGS)
    List<FieldMappingTo> getFieldMappings();

    @PostMapping(RoutesTo.FIELD_MAPPINGS)
    FieldMappingTo createFieldMapping(@RequestBody FieldMappingRequestTo request);

    @PostMapping(RoutesTo.DISCORD_ALL_MESSAGES)
    void postMessage(@RequestBody MessageLogTo messageLog);

    @GetMapping(RoutesTo.DISCORD_MESSAGE_DETAILS)
    MessageLogTo getMessageDetails(@RequestParam long messageId);

    @GetMapping(RoutesTo.DISCORD_MESSAGE)
    boolean hasMessage(@RequestParam long messageId, @RequestParam MessageType messageType);
}
