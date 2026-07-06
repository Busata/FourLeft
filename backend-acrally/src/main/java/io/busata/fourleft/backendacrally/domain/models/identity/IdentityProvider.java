package io.busata.fourleft.backendacrally.domain.models.identity;

/**
 * External identity providers a user can link. Steam is the first (and, for now, the
 * anti-abuse anchor); others (Discord, Epic, …) slot in without schema changes.
 */
public enum IdentityProvider {
    STEAM
}
