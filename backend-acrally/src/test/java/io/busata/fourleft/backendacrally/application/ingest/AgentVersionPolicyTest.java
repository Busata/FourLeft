package io.busata.fourleft.backendacrally.application.ingest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentVersionPolicyTest {

    private final AgentVersionPolicy policy = new AgentVersionPolicy("0.3.8");

    @Test
    void acceptsTheMinimumAndNewer() {
        assertThatCode(() -> policy.requireSupported("0.3.8")).doesNotThrowAnyException();
        assertThatCode(() -> policy.requireSupported("0.3.10")).doesNotThrowAnyException();
        assertThatCode(() -> policy.requireSupported("1.0.0")).doesNotThrowAnyException();
    }

    @Test
    void rejectsOlderWith426() {
        assertThatThrownBy(() -> policy.requireSupported("0.3.7"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UPGRADE_REQUIRED));
    }

    @Test
    void rejectsMissingOrGarbageVersions() {
        assertThatThrownBy(() -> policy.requireSupported(null))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> policy.requireSupported("dev"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void numericCompareNotLexicographic() {
        // "0.3.10" < "0.3.8" lexicographically — must not reject it.
        assertThat(AgentVersionPolicy.compare("0.3.10", "0.3.8")).isPositive();
        assertThat(AgentVersionPolicy.compare("0.3.8", "0.3.8.1")).isNegative();
    }

    @Test
    void blankMinimumDisablesTheGate() {
        AgentVersionPolicy off = new AgentVersionPolicy("");
        assertThatCode(() -> off.requireSupported("0.0.1")).doesNotThrowAnyException();
        assertThatCode(() -> off.requireSupported(null)).doesNotThrowAnyException();
    }
}
