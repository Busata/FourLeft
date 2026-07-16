package io.busata.fourleft.backendacrally.domain.services.issue;

import io.busata.fourleft.backendacrally.domain.models.issue.IssueReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Submission guards. Pure Mockito — no Spring context or DB needed. */
@ExtendWith(MockitoExtension.class)
class IssueReportServiceTest {

    @Mock IssueReportRepository repository;

    @InjectMocks IssueReportService service;

    private final UUID userId = UUID.randomUUID();

    private IssueReport submit(String description, byte[] save, byte[] log) {
        return service.submit(userId, description, "0.4.0",
                save, save == null ? null : "PlayerDataSaveSlot.sav",
                log, log == null ? null : "agent.log");
    }

    @Test
    void storesReportWithAttachmentSizes() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0, IssueReport.class));

        IssueReport report = submit("  Results stopped posting after the update.  ",
                new byte[]{1, 2, 3}, new byte[]{4});

        assertThat(report.getDescription()).isEqualTo("Results stopped posting after the update.");
        assertThat(report.getUserId()).isEqualTo(userId);
        assertThat(report.getSaveGameSize()).isEqualTo(3);
        assertThat(report.getAgentLogSize()).isEqualTo(1);
    }

    @Test
    void attachmentsAreOptional() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0, IssueReport.class));

        IssueReport report = submit("The agent can't find my save file.", null, null);

        assertThat(report.getSaveGame()).isNull();
        assertThat(report.getSaveGameSize()).isZero();
        assertThat(report.getAgentLog()).isNull();
    }

    @Test
    void rejectsEmptyDescription() {
        assertThatThrownBy(() -> submit("   ", null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("describe");
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsOversizedAttachment() {
        byte[] oversized = new byte[IssueReportService.MAX_ATTACHMENT_BYTES + 1];

        assertThatThrownBy(() -> submit("Save attached.", oversized, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("too large");
        verify(repository, never()).save(any());
    }

    @Test
    void rateLimitsPerUserPerDay() {
        when(repository.countByUserIdAndCreatedAtAfter(eq(userId), any()))
                .thenReturn((long) IssueReportService.MAX_REPORTS_PER_DAY);

        assertThatThrownBy(() -> submit("Yet another report.", null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Too many reports");
        verify(repository, never()).save(any());
    }
}
