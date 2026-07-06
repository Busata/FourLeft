package io.busata.fourleft.backendacrally.application.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The acrally-agent wire contract (see repo-root API_CONTRACT.md). Snake_case JSON is mapped with
 * {@code @JsonProperty} rather than a global naming strategy so the browser-facing DTOs stay camelCase.
 */
public final class IngestPayloads {

    private IngestPayloads() {
    }

    public record SessionStart(
            String driver,
            String car,
            String stage,
            String track,
            @JsonProperty("driver_id") String driverId,
            @JsonProperty("club_id") String clubId,
            @JsonProperty("started_at_ms") Long startedAtMs,
            @JsonProperty("agent_version") String agentVersion) {
    }

    public record Heartbeat(
            @JsonProperty("current_ms") Integer currentMs,
            @JsonProperty("speed_kmh") Double speedKmh,
            Integer gear,
            Integer rpm,
            @JsonProperty("distance_m") Double distanceM) {
    }

    public record Result(
            String stage,
            String car,
            String driver,
            @JsonProperty("raw_ms") int rawMs,
            @JsonProperty("penalty_ms") int penaltyMs,
            @JsonProperty("total_ms") int totalMs,
            @JsonProperty("timestamp_ticks") long timestampTicks,
            @JsonProperty("driver_id") String driverId,
            @JsonProperty("club_id") String clubId,
            @JsonProperty("agent_version") String agentVersion) {
    }

    public record Abort(String reason) {
    }

    /** Response to POST /sessions — the agent reads {@code session_id} (or {@code id}). */
    public record SessionOpened(@JsonProperty("session_id") String sessionId) {
    }

    /** Body-ignored acknowledgement for heartbeat/result/abort (contract only needs a 2xx). */
    public record Ok(boolean ok) {
    }
}
