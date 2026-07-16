package io.busata.fourleft.backendacrally.application.ingest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Mandatory-update gate for the ingestion endpoints. Agents older than
 * {@code acrally.agent.min-version} are rejected with 426 Upgrade Required, which the agent
 * treats as "self-update now" — old agents drift out of the wire contract (missing fields,
 * fixed parser bugs) and would otherwise keep feeding mismatched data forever. Bump the
 * property whenever the contract changes; blank disables the gate.
 */
@Component
public class AgentVersionPolicy {

    private final String minVersion;

    public AgentVersionPolicy(@Value("${acrally.agent.min-version:}") String minVersion) {
        this.minVersion = minVersion == null ? "" : minVersion.strip();
    }

    /** Throws 426 when {@code agentVersion} is older than the configured minimum. */
    public void requireSupported(String agentVersion) {
        if (minVersion.isBlank()) {
            return;
        }
        if (compare(agentVersion, minVersion) < 0) {
            throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED,
                    "Agent %s is no longer supported (minimum %s) — restart the agent so it updates itself."
                            .formatted(agentVersion == null || agentVersion.isBlank() ? "(unknown)" : agentVersion,
                                    minVersion));
        }
    }

    /**
     * Dotted-numeric version compare ("0.3.10" > "0.3.8"). Missing or non-numeric parts count
     * as 0, so a blank or garbage agent version compares below any real minimum.
     */
    static int compare(String left, String right) {
        String[] a = left == null ? new String[0] : left.strip().split("\\.");
        String[] b = right == null ? new String[0] : right.strip().split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int cmp = Integer.compare(numericPart(a, i), numericPart(b, i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    private static int numericPart(String[] parts, int i) {
        if (i >= parts.length) {
            return 0;
        }
        try {
            return Integer.parseInt(parts[i]);
        } catch (NumberFormatException notNumeric) {
            return 0;
        }
    }
}
