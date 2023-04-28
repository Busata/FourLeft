package io.busata.fourleft.endpoints;

import io.busata.fourleft.endpoints.discord.integration.exceptions.DiscordNotAuthenticatedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class EndpointExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DiscordNotAuthenticatedException.class)
    public final ResponseEntity<Object> handleDiscordNotAuthenticatedException(DiscordNotAuthenticatedException ex, WebRequest request) {
        return handleExceptionInternal(ex, "Not authenticated for discord", new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }
}
