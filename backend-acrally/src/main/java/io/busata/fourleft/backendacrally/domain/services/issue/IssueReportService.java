package io.busata.fourleft.backendacrally.domain.services.issue;

import io.busata.fourleft.backendacrally.domain.models.issue.IssueReport;
import io.busata.fourleft.backendacrally.domain.services.issue.IssueReportRepository.IssueReportSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * User-submitted problem reports (description + save game + agent log). Attachments are optional —
 * a report is still useful when the agent couldn't read one of the files — but bounded, and
 * submissions are rate-limited per user so the issues endpoint can't be used to fill the database.
 */
@Service
@RequiredArgsConstructor
public class IssueReportService {

    /** Upper bound per attachment (decoded). The save is ~1 MB, the rotated log ≤ 1 MB. */
    static final int MAX_ATTACHMENT_BYTES = 16 * 1024 * 1024;
    static final int MAX_DESCRIPTION_CHARS = 4000;
    /** At most this many reports per user per day — abuse brake, generous for real debugging. */
    static final int MAX_REPORTS_PER_DAY = 10;

    private final IssueReportRepository repository;

    public IssueReport submit(UUID userId, String description, String agentVersion,
                              byte[] saveGame, String saveGameName, byte[] agentLog, String agentLogName) {
        String trimmed = description == null ? "" : description.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Please describe the issue.");
        }
        if (trimmed.length() > MAX_DESCRIPTION_CHARS) {
            trimmed = trimmed.substring(0, MAX_DESCRIPTION_CHARS);
        }
        requireWithin(saveGame, "save game");
        requireWithin(agentLog, "agent log");
        if (repository.countByUserIdAndCreatedAtAfter(userId, LocalDateTime.now().minusDays(1))
                >= MAX_REPORTS_PER_DAY) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many reports today — please try again tomorrow.");
        }
        return repository.save(new IssueReport(userId, trimmed, agentVersion,
                saveGame, saveGameName, agentLog, agentLogName));
    }

    public List<IssueReportSummary> list() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public IssueReport get(UUID id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown issue report."));
    }

    public void delete(UUID id) {
        repository.delete(get(id));
    }

    private void requireWithin(byte[] attachment, String label) {
        if (attachment != null && attachment.length > MAX_ATTACHMENT_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "The " + label + " attachment is too large.");
        }
    }
}
