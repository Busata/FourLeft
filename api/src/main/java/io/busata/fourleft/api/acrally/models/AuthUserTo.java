package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/**
 * The authenticated user as seen by the browser. Identity is the Steam sign-in; the
 * browser only ever needs the stable id and the (user-configurable) display name.
 */
public record AuthUserTo(UUID id, String displayName, String status, boolean admin) {
}
