package io.busata.fourleft.backendacrally.application.races;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * The wire contract for the agent's Races tab (the "arm & register" flow). Like {@code IngestPayloads}
 * these are agent-only and use snake_case via {@code @JsonProperty} so the browser DTOs stay camelCase.
 * The agent reads {@link RacesView} to render the list, and {@link ArmState} to drive its Start button
 * and live warnings.
 */
public final class RacePayloads {

    private RacePayloads() {
    }

    /** Everything the agent needs on one poll: the open events plus the driver's current arm state. */
    public record RacesView(List<RaceEvent> events, ArmState arm) {
    }

    /** An event currently open for entries, in a club the driver belongs to. */
    public record RaceEvent(
            @JsonProperty("event_id") UUID eventId,
            @JsonProperty("championship_id") UUID championshipId,
            @JsonProperty("championship_name") String championshipName,
            @JsonProperty("club_name") String clubName,
            @JsonProperty("label") String label,
            @JsonProperty("opens_at") String opensAt,
            @JsonProperty("closes_at") String closesAt,
            @JsonProperty("stages") List<RaceStage> stages) {
    }

    /** A stage a driver can arm, with the raw key + car list the agent uses for live warnings. */
    public record RaceStage(
            @JsonProperty("variant_id") UUID variantId,
            @JsonProperty("raw_name") String rawName,
            @JsonProperty("label") String label,
            @JsonProperty("stage_name") String stageName,
            @JsonProperty("location_name") String locationName,
            @JsonProperty("cars") List<String> cars,
            @JsonProperty("my_best_ms") Integer myBestMs) {
    }

    /** POST body to arm a stage. */
    public record ArmRequest(
            @JsonProperty("event_id") UUID eventId,
            @JsonProperty("variant_id") UUID variantId) {
    }

    /**
     * The driver's arm status. When {@code active}, a run is expected (ARMED/BOUND) and the expected
     * stage/cars are populated for live warnings. When not active, {@code lastOutcome} carries how the
     * previous run scored so the tab can show "✓ Recorded" / "⚠ wrong stage".
     */
    public record ArmState(
            @JsonProperty("active") boolean active,
            @JsonProperty("status") String status,
            @JsonProperty("event_id") UUID eventId,
            @JsonProperty("variant_id") UUID variantId,
            @JsonProperty("stage_label") String stageLabel,
            @JsonProperty("raw_name") String rawName,
            @JsonProperty("cars") List<String> cars,
            @JsonProperty("last_outcome") String lastOutcome,
            @JsonProperty("last_stage_label") String lastStageLabel,
            @JsonProperty("last_total_ms") Integer lastTotalMs) {

        public static ArmState idle() {
            return new ArmState(false, null, null, null, null, null, List.of(), null, null, null);
        }
    }
}
