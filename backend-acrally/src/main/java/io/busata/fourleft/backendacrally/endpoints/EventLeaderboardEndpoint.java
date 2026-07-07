package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.EventLeaderboardTo;
import io.busata.fourleft.api.acrally.models.EventStandingTo;
import io.busata.fourleft.api.acrally.models.LeaderboardEntryTo;
import io.busata.fourleft.api.acrally.models.StageBoardTo;
import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipEvent;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipStatus;
import io.busata.fourleft.backendacrally.domain.services.championship.ChampionshipEventRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.ChampionshipService;
import io.busata.fourleft.backendacrally.domain.services.championship.EventLeaderboardService;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantService;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-only event leaderboards for the browser (session-authed). Visible to any signed-in user for a
 * published championship; a draft's boards stay hidden from non-owners, mirroring the schedule view.
 */
@RestController
@RequestMapping("/acrally-api")
@RequiredArgsConstructor
public class EventLeaderboardEndpoint {

    private final ChampionshipEventRepository eventRepository;
    private final ChampionshipService championshipService;
    private final EventLeaderboardService leaderboardService;
    private final VariantService variantService;

    @GetMapping("/events/{eventId}/leaderboard")
    public EventLeaderboardTo leaderboard(@PathVariable UUID eventId,
                                          @AuthenticationPrincipal AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        ChampionshipEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such event."));
        Championship championship = championshipService.get(event.getChampionshipId());
        boolean owner = championshipService.isOwner(championship, principal.getId());
        if (championship.getStatus() == ChampionshipStatus.DRAFT && !owner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such event.");
        }

        Map<UUID, VariantService.VariantLabel> labels = variantService.labelsById();
        EventLeaderboardService.EventStandings standings = leaderboardService.standings(eventId);

        List<StageBoardTo> stages = standings.stages().stream()
                .map(board -> toStageBoard(board, labels))
                .toList();

        List<EventStandingTo> overall = rank(standings.overall());
        String label = deriveLabel(stages);
        return new EventLeaderboardTo(eventId, label, stages.size(), stages, overall);
    }

    private StageBoardTo toStageBoard(EventLeaderboardService.StageBoard board,
                                      Map<UUID, VariantService.VariantLabel> labels) {
        VariantService.VariantLabel label = labels.get(board.variantId());
        List<LeaderboardEntryTo> entries = new java.util.ArrayList<>();
        List<EventLeaderboardService.BoardRow> rows = board.rows();
        for (int i = 0; i < rows.size(); i++) {
            EventLeaderboardService.BoardRow r = rows.get(i);
            entries.add(new LeaderboardEntryTo(i + 1, r.userId(), r.driver(), r.carName(),
                    r.rawMs(), r.penaltyMs(), r.totalMs(), r.recordedAt()));
        }
        return new StageBoardTo(
                board.variantId(),
                label == null ? "(stage)" : label.label(),
                label == null ? null : label.stageName(),
                label == null ? null : label.locationName(),
                entries);
    }

    private List<EventStandingTo> rank(List<EventLeaderboardService.Standing> standings) {
        List<EventStandingTo> ranked = new java.util.ArrayList<>();
        for (int i = 0; i < standings.size(); i++) {
            EventLeaderboardService.Standing s = standings.get(i);
            ranked.add(new EventStandingTo(i + 1, s.userId(), s.driver(), s.totalMs(), s.stagesCompleted()));
        }
        return ranked;
    }

    /** Label the event by the distinct locations of its stages (running order preserved). */
    private String deriveLabel(List<StageBoardTo> stages) {
        String label = stages.stream()
                .map(StageBoardTo::locationName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(" · "));
        return label.isBlank() ? "Event" : label;
    }
}
