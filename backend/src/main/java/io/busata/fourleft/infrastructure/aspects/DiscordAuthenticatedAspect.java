package io.busata.fourleft.infrastructure.aspects;

import io.busata.fourleft.application.discord.DiscordIntegrationService;
import io.busata.fourleft.application.discord.exceptions.DiscordNotAuthenticatedException;
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

    @Around("@annotation(io.busata.fourleft.infrastructure.aspects.DiscordAuthenticated)")
    public Object checkAccess(ProceedingJoinPoint joinPoint) throws Throwable {

        if (discordIntegrationService.isAuthenticated()) {
            return joinPoint.proceed();
        }

        throw new DiscordNotAuthenticatedException("Not authenticated for discord");
    }
}
