package io.busata.fourleft.backendacrally.domain.services.issue;

import io.busata.fourleft.backendacrally.domain.models.issue.IssueReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IssueReportRepository extends JpaRepository<IssueReport, UUID> {

    /**
     * Blob-free row for the admin list. Interface projection: Spring Data selects only these
     * properties, so listing reports never drags the save game / log bytes out of the database.
     */
    interface IssueReportSummary {
        UUID getId();

        UUID getUserId();

        String getDescription();

        String getAgentVersion();

        String getSaveGameName();

        int getSaveGameSize();

        String getAgentLogName();

        int getAgentLogSize();

        LocalDateTime getCreatedAt();
    }

    List<IssueReportSummary> findAllByOrderByCreatedAtDesc();

    long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime after);
}
