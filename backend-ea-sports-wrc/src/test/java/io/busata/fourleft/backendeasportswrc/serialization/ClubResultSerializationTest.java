package io.busata.fourleft.backendeasportswrc.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.busata.fourleft.api.easportswrc.models.ClubResultEntryTo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins how a club result's java.time.Duration times serialize over the wire, so the club-compare
 * frontend's parseDuration() stays aligned with what the backend actually emits.
 *
 * Uses an ApplicationContextRunner with only JacksonAutoConfiguration — it builds the exact same
 * ObjectMapper Spring Boot injects app-wide (the app adds no custom Jackson config), but WITHOUT the
 * main application context, so there's no Feign/RabbitMQ/DB to stand up. That makes it runnable in a
 * bare environment, unlike the @SpringBootTest integration suite. The cached club summary is written
 * and served with this same mapper, so this is exactly what /api_v2/cached/club_summary returns.
 */
@Slf4j
class ClubResultSerializationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class));

    @Test
    void durationSerializesInAFormatTheFrontendCanParse() {
        contextRunner.run(context -> {
            ObjectMapper mapper = context.getBean(ObjectMapper.class);

            Duration lastStage = Duration.ofMinutes(1).plusSeconds(23).plusMillis(456); // 83.456s
            ClubResultEntryTo entry = new ClubResultEntryTo(
                    "wrc-1", "Driver One", 1L, 0L, 1L, "Rally1",
                    lastStage,
                    Duration.ofMinutes(5).plusSeconds(7).plusMillis(890),
                    Duration.ZERO, Duration.ZERO, Duration.ZERO, null);

            String json = mapper.writeValueAsString(entry);
            log.info("ClubResultEntryTo serialized as: {}", json);

            JsonNode time = mapper.readTree(json).get("time");
            log.info("time field node = {} (numeric={})", time, time.isNumber());

            // Whatever shape Jackson emits, it must be one the frontend parseDuration() accepts:
            //   - a JSON number of fractional SECONDS (not millis/nanos), or
            //   - an ISO-8601 "PT..." string, or a bare numeric / clock string.
            boolean parseable;
            if (time.isNumber()) {
                // Frontend treats a number as seconds; verify that assumption (83.456s, not 83456ms).
                parseable = Math.abs(time.asDouble() - 83.456) < 0.001;
            } else {
                String text = time.asText();
                parseable = text.startsWith("PT") || text.matches("[-+]?\\d+(\\.\\d+)?") || text.contains(":");
            }

            assertThat(parseable)
                    .as("time serialized as <%s> — frontend parseDuration() must handle this exact shape", time)
                    .isTrue();
        });
    }
}
