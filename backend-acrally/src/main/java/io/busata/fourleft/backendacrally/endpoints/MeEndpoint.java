package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.MyResultTo;
import io.busata.fourleft.api.acrally.models.MySessionTo;
import io.busata.fourleft.backendacrally.domain.services.session.AgentSessionRepository;
import io.busata.fourleft.backendacrally.domain.services.session.StageResultRepository;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** The signed-in user's own driving data — the personal dashboard. Session (cookie) authenticated. */
@RestController
@RequestMapping("/acrally-api/me")
@RequiredArgsConstructor
public class MeEndpoint {

    private final AgentSessionRepository sessionRepository;
    private final StageResultRepository resultRepository;

    @GetMapping("/sessions")
    public List<MySessionTo> mySessions(@AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        return sessionRepository.findTop50ByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(session -> new MySessionTo(
                        session.getId(),
                        session.getDriver(),
                        session.getCar(),
                        session.getStage(),
                        session.getTrack(),
                        session.getStatus().name(),
                        session.getStartedAtMs(),
                        session.getCurrentMs(),
                        session.getLastHeartbeatAt(),
                        session.getCreatedAt()))
                .toList();
    }

    @GetMapping("/results")
    public List<MyResultTo> myResults(@AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        return resultRepository.findTop100ByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(result -> new MyResultTo(
                        result.getId(),
                        result.getStage(),
                        result.getCar(),
                        result.getDriver(),
                        result.getRawMs(),
                        result.getPenaltyMs(),
                        result.getTotalMs(),
                        result.getCreatedAt()))
                .toList();
    }

    private void requireLogin(AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
