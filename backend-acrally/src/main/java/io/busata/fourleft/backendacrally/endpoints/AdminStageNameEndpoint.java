package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.StageNameCollectResultTo;
import io.busata.fourleft.api.acrally.models.StageNameTo;
import io.busata.fourleft.api.acrally.models.UpdateStageNameRequestTo;
import io.busata.fourleft.backendacrally.domain.models.stage.StageName;
import io.busata.fourleft.backendacrally.domain.services.stage.StageNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Administration surface for the stage-name catalogue. The whole {@code /acrally-api/admin/**}
 * tree is locked to ROLE_ADMIN in {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/acrally-api/admin/stage-names")
@RequiredArgsConstructor
public class AdminStageNameEndpoint {

    private final StageNameService stageNameService;

    @GetMapping("")
    public List<StageNameTo> list() {
        return stageNameService.list().stream().map(this::toTo).toList();
    }

    /** Scans results for distinct raw stage names and catalogues any new ones (existing rows untouched). */
    @PostMapping("/collect")
    public StageNameCollectResultTo collect() {
        int added = stageNameService.collect();
        List<StageNameTo> stages = stageNameService.list().stream().map(this::toTo).toList();
        return new StageNameCollectResultTo(added, stages);
    }

    /** Assigns (or clears, when blank) the readable display name for a catalogued stage. */
    @PutMapping("/{id}")
    public StageNameTo update(@PathVariable UUID id, @RequestBody UpdateStageNameRequestTo request) {
        return toTo(stageNameService.rename(id, request.displayName()));
    }

    private StageNameTo toTo(StageName stageName) {
        return new StageNameTo(
                stageName.getId(),
                stageName.getRawName(),
                stageName.getDisplayName(),
                stageName.getCreatedAt(),
                stageName.getUpdatedAt());
    }
}
