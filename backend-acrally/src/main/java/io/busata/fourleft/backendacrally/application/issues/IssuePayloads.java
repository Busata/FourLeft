package io.busata.fourleft.backendacrally.application.issues;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The acrally-agent "submit issue" wire contract (agent-side counterpart: {@code src/issue.rs}).
 * Snake_case JSON mapped with {@code @JsonProperty}, like {@code IngestPayloads}. Attachments travel
 * base64-encoded inside the JSON body — the agent has no multipart client, and the files are small
 * (the save is ~1 MB, the log is rotated at 1 MB).
 */
public final class IssuePayloads {

    private IssuePayloads() {
    }

    public record Submit(
            String description,
            @JsonProperty("agent_version") String agentVersion,
            @JsonProperty("save_game_b64") String saveGameB64,
            @JsonProperty("save_game_name") String saveGameName,
            @JsonProperty("log_b64") String logB64,
            @JsonProperty("log_name") String logName) {
    }

    /** Response to POST /agent/issues — the report id, mainly so the user can reference it. */
    public record Submitted(@JsonProperty("issue_id") String issueId) {
    }
}
