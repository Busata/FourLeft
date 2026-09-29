package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.events.ProfileUpdatedEvent;
import io.busata.fourleft.api.easportswrc.models.ProfileDisputeAdminTo;
import io.busata.fourleft.backendeasportswrc.application.admin.ProfileDisputeAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Operator page for profile disputes; the admin link id is the credential (404 when unknown or expired). */
@RestController
@RequiredArgsConstructor
public class ProfileDisputeAdminEndpoint {
    private final ProfileDisputeAdminService service;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/api_v2/admin/{linkId}/disputes")
    public List<ProfileDisputeAdminTo> openDisputes(@PathVariable UUID linkId) {
        return service.openDisputes(linkId);
    }

    @PostMapping("/api_v2/admin/{linkId}/disputes/{playerId}/dismiss")
    public List<ProfileDisputeAdminTo> dismiss(@PathVariable UUID linkId, @PathVariable String playerId) {
        return service.dismiss(linkId, playerId);
    }

    @PostMapping("/api_v2/admin/{linkId}/disputes/{playerId}/transfer")
    public List<ProfileDisputeAdminTo> transfer(@PathVariable UUID linkId, @PathVariable String playerId) {
        List<ProfileDisputeAdminTo> disputes = service.transfer(linkId, playerId);
        eventPublisher.publishEvent(new ProfileUpdatedEvent());
        return disputes;
    }
}
