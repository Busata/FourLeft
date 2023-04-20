package io.busata.fourleft.endpoints.discord.integration;

import io.busata.fourleft.endpoints.discord.integration.exceptions.DiscordNotAuthenticatedException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class DiscordAuthenticatedAspect {

    private final DiscordIntegrationService discordIntegrationService;

    @Around("@annotation(io.busata.fourleft.endpoints.discord.integration.DiscordAuthenticated)")
    public Object checkAccess(ProceedingJoinPoint joinPoint) throws Throwable {

        if (discordIntegrationService.isAuthenticated()) {
            return joinPoint.proceed();
        }

        throw new DiscordNotAuthenticatedException("Not authenticated for discord");
    }
}
