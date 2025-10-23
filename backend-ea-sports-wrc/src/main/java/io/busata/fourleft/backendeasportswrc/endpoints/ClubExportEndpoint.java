package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.ClubOverviewTo;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubExportConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.services.clubExport.ClubExportConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.services.clubExport.ClubExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClubExportEndpoint {

    private final ClubExportService clubExportService;
    private final ClubExportConfigurationService clubExportConfigurationService;

    @GetMapping("/api_v2/cached/club_summary/{clubId}")
    public ResponseEntity<ClubOverviewTo> getCachedClubSummary(@PathVariable String clubId) {
        return clubExportService.getCachedClub(clubId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api_v2/export/clubs")
    public List<ClubExportConfiguration> listExportConfigurations() {
        return clubExportConfigurationService.findAllExportConfigurations();
    }

    @PostMapping("/api_v2/export/clubs/{clubId}")
    public ClubExportConfiguration addClubToExport(@PathVariable String clubId) {
        return clubExportConfigurationService.addClubToExport(clubId);
    }

    @DeleteMapping("/api_v2/export/clubs/{clubId}")
    public ResponseEntity<Void> removeClubFromExport(@PathVariable String clubId) {
        clubExportConfigurationService.removeClubFromExport(clubId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api_v2/export/clubs/{clubId}/enable")
    public ResponseEntity<Void> enableClubExport(@PathVariable String clubId) {
        clubExportConfigurationService.setClubExportEnabled(clubId, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api_v2/export/clubs/{clubId}/disable")
    public ResponseEntity<Void> disableClubExport(@PathVariable String clubId) {
        clubExportConfigurationService.setClubExportEnabled(clubId, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api_v2/export/trigger")
    public ResponseEntity<Void> triggerExportAll() {
        log.info("Manual trigger of all club exports");
        clubExportService.exportAllEnabledClubs();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api_v2/export/clubs/{clubId}/trigger")
    public ResponseEntity<Void> triggerExportClub(@PathVariable String clubId) {
        log.info("Manual trigger of export for club {}", clubId);
        clubExportService.exportClub(clubId);
        return ResponseEntity.ok().build();
    }
}
