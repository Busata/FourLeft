package io.busata.fourleft.endpoints.frontend.discord_integration.exceptions;

public class DiscordNotAuthenticatedException extends RuntimeException {

    public DiscordNotAuthenticatedException(String message) {
        super(message);
    }
}
