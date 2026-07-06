package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.backendacrally.application.ingest.IngestPayloads;
import io.busata.fourleft.backendacrally.application.ingest.SessionIngestService;
import io.busata.fourleft.backendacrally.infrastructure.security.AgentPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * The four API_CONTRACT.md ingestion endpoints. Authenticated by the agent's API key (Bearer),
 * resolved to an {@link AgentPrincipal} by {@code ApiKeyAuthFilter}. Session ids are server-issued.
 */
@RestController
@RequestMapping("/acrally-api/sessions")
@RequiredArgsConstructor
public class SessionEndpoint {

    private final SessionIngestService ingestService;

    @PostMapping
    public IngestPayloads.SessionOpened open(@AuthenticationPrincipal AgentPrincipal agent,
                                             @RequestBody IngestPayloads.SessionStart payload) {
        UUID sessionId = ingestService.open(agent.userId(), agent.apiKeyId(), payload);
        return new IngestPayloads.SessionOpened(sessionId.toString());
    }

    @PostMapping("/{id}/heartbeat")
    public IngestPayloads.Ok heartbeat(@AuthenticationPrincipal AgentPrincipal agent,
                                       @PathVariable String id,
                                       @RequestBody IngestPayloads.Heartbeat payload) {
        ingestService.heartbeat(agent.userId(), id, payload);
        return new IngestPayloads.Ok(true);
    }

    @PostMapping("/{id}/result")
    public IngestPayloads.Ok result(@AuthenticationPrincipal AgentPrincipal agent,
                                    @PathVariable String id,
                                    @RequestBody IngestPayloads.Result payload) {
        ingestService.recordResult(agent.userId(), id, payload);
        return new IngestPayloads.Ok(true);
    }

    @PostMapping("/{id}/abort")
    public IngestPayloads.Ok abort(@AuthenticationPrincipal AgentPrincipal agent,
                                   @PathVariable String id,
                                   @RequestBody(required = false) IngestPayloads.Abort payload) {
        ingestService.abort(agent.userId(), id, payload == null ? null : payload.reason());
        return new IngestPayloads.Ok(true);
    }
}
