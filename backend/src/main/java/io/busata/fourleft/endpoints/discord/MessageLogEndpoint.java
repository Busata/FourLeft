package io.busata.fourleft.endpoints.discord;


import io.busata.fourleft.api.Routes;
import io.busata.fourleft.common.MessageType;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.application.discord.MessageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageLogEndpoint {

    private final MessageLogService messageLogService;

    @PostMapping(Routes.DISCORD_ALL_MESSAGES)
    public void postMessage(@RequestBody MessageLogTo request) {
        messageLogService.postMessage(request);
    }

    @GetMapping(Routes.DISCORD_MESSAGE_DETAILS)
    public MessageLogTo getMessage(@PathVariable long messageId) {
        return messageLogService.getMessageById(messageId);
    }

    @GetMapping(Routes.DISCORD_ALL_MESSAGES)
    public List<MessageLogTo> getMessages() {
        return messageLogService.getAllMessages();
    }

    @GetMapping(Routes.DISCORD_MESSAGE)
    public boolean getMessages(@RequestParam long messageId, @RequestParam MessageType messageType) {
        return messageLogService.getMessageByType(messageId, messageType);
    }
}
