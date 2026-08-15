package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.EventDnfTo;
import io.busata.fourleft.backendacrally.domain.services.championship.EventDnfService;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * The club owner's DNF panel for an event: who lost their shot at a stage, and the revert that
 * hands it back after a technical mishap. Owner-gated in {@link EventDnfService} (403 for anyone
 * else); the revert returns the refreshed list so the panel rebinds without a second round-trip.
 */
@RestController
@RequestMapping("/acrally-api/events/{eventId}/dnfs")
@RequiredArgsConstructor
public class EventDnfEndpoint {

    private final EventDnfService dnfService;

    @GetMapping
    public List<EventDnfTo> list(@PathVariable UUID eventId,
                                 @AuthenticationPrincipal AppUserDetails principal) {
        return toTos(dnfService.list(eventId, requireLogin(principal)));
    }

    @PostMapping("/{armId}/revert")
    public List<EventDnfTo> revert(@PathVariable UUID eventId, @PathVariable UUID armId,
                                   @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        dnfService.revert(eventId, armId, userId);
        return toTos(dnfService.list(eventId, userId));
    }

    private List<EventDnfTo> toTos(List<EventDnfService.DnfRow> rows) {
        return rows.stream()
                .map(r -> new EventDnfTo(r.armId(), r.userId(), r.driver(), r.variantId(), r.stageLabel(),
                        r.cause().name(), r.occurredAt(), r.revertedAt(), r.revertedBy(), r.hasTimeSince()))
                .toList();
    }

    private UUID requireLogin(AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return principal.getId();
    }
}
