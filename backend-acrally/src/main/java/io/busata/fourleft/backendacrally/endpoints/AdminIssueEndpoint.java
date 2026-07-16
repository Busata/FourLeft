package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.AdminIssueReportTo;
import io.busata.fourleft.backendacrally.domain.models.issue.IssueReport;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.issue.IssueReportRepository.IssueReportSummary;
import io.busata.fourleft.backendacrally.domain.services.issue.IssueReportService;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin view of user-submitted issue reports: list them, download the attached save game / agent
 * log, delete handled ones. Locked to ROLE_ADMIN by the {@code /acrally-api/admin/**} rule.
 */
@RestController
@RequestMapping("/acrally-api/admin/issues")
@RequiredArgsConstructor
public class AdminIssueEndpoint {

    private final IssueReportService issueReportService;
    private final AppUserRepository userRepository;

    @GetMapping("")
    public List<AdminIssueReportTo> list() {
        List<IssueReportSummary> summaries = issueReportService.list();
        Set<UUID> userIds = summaries.stream().map(IssueReportSummary::getUserId)
                .collect(Collectors.toSet());
        Map<UUID, String> names = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getDisplayName));
        return summaries.stream().map(summary -> toTo(summary, names)).toList();
    }

    @GetMapping("/{id}/savegame")
    public ResponseEntity<byte[]> downloadSaveGame(@PathVariable UUID id) {
        IssueReport report = issueReportService.get(id);
        return download(report.getSaveGame(), report.getSaveGameName(), "savegame.sav", id);
    }

    @GetMapping("/{id}/log")
    public ResponseEntity<byte[]> downloadLog(@PathVariable UUID id) {
        IssueReport report = issueReportService.get(id);
        return download(report.getAgentLog(), report.getAgentLogName(), "agent.log", id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        issueReportService.delete(id);
    }

    private ResponseEntity<byte[]> download(byte[] bytes, String name, String fallbackName, UUID id) {
        if (bytes == null || bytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This report has no such attachment.");
        }
        // Prefix the report id so downloads from different reports don't overwrite each other.
        String filename = id + "-" + (name == null || name.isBlank() ? fallbackName : name);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(bytes);
    }

    private AdminIssueReportTo toTo(IssueReportSummary summary, Map<UUID, String> names) {
        return new AdminIssueReportTo(
                summary.getId(),
                summary.getUserId(),
                names.getOrDefault(summary.getUserId(), "(unknown)"),
                summary.getDescription(),
                summary.getAgentVersion(),
                summary.getSaveGameName(),
                summary.getSaveGameSize(),
                summary.getAgentLogName(),
                summary.getAgentLogSize(),
                summary.getCreatedAt());
    }
}
