package io.busata.fourleft.application.discord.exceptions;

public class DiscordNotAuthenticatedException extends RuntimeException {

    public DiscordNotAuthenticatedException(String message) {
        super(message);
    }
}
