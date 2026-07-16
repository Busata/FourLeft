package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.backendacrally.application.issues.IssuePayloads;
import io.busata.fourleft.backendacrally.domain.models.issue.IssueReport;
import io.busata.fourleft.backendacrally.domain.services.issue.IssueReportService;
import io.busata.fourleft.backendacrally.infrastructure.security.AgentPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

/**
 * The companion's "Report issue" button, API-key authenticated (an {@link AgentPrincipal}) like the
 * ingestion endpoints. The save game + agent log arrive base64-encoded in the JSON body; size and
 * rate limits live in {@link IssueReportService}.
 */
@RestController
@RequestMapping("/acrally-api/agent/issues")
@RequiredArgsConstructor
public class AgentIssueEndpoint {

    private final IssueReportService issueReportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssuePayloads.Submitted submit(@AuthenticationPrincipal AgentPrincipal agent,
                                          @RequestBody IssuePayloads.Submit payload) {
        IssueReport report = issueReportService.submit(
                agent.userId(),
                payload.description(),
                payload.agentVersion(),
                decode(payload.saveGameB64(), "save_game_b64"),
                payload.saveGameName(),
                decode(payload.logB64(), "log_b64"),
                payload.logName());
        return new IssuePayloads.Submitted(report.getId().toString());
    }

    private byte[] decode(String b64, String field) {
        if (b64 == null || b64.isBlank()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid base64 in " + field + ".");
        }
    }
}
