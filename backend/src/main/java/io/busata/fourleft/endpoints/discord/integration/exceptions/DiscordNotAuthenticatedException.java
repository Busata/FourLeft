package io.busata.fourleft.endpoints.discord.integration.exceptions;

public class DiscordNotAuthenticatedException extends RuntimeException {

    public DiscordNotAuthenticatedException(String message) {
        super(message);
    }
}
