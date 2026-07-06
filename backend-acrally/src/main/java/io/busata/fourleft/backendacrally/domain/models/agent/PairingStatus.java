package io.busata.fourleft.backendacrally.domain.models.agent;

public enum PairingStatus {
    /** Started by the agent, awaiting user approval. */
    PENDING,
    /** User approved in the browser; the agent's next poll mints the key. */
    APPROVED,
    /** The agent has exchanged it for a key — terminal, single-use. */
    CONSUMED,
    /** User declined. */
    DENIED
}
