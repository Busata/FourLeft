package io.busata.fourleft.backendacrally.domain.models.issue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A problem report submitted from the companion agent: a free-text description plus the save game
 * and agent log captured at submit time. The blobs live on the row (bytea); listings must go through
 * the summary projection ({@code IssueReportRepository#findSummaries}) so they never load them.
 */
@Entity
@Table(name = "issue_report")
@Getter
@NoArgsConstructor
public class IssueReport {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String description;

    @Column(name = "agent_version")
    private String agentVersion;

    @Column(name = "save_game")
    private byte[] saveGame;

    @Column(name = "save_game_name")
    private String saveGameName;

    @Column(name = "save_game_size", nullable = false)
    private int saveGameSize;

    @Column(name = "agent_log")
    private byte[] agentLog;

    @Column(name = "agent_log_name")
    private String agentLogName;

    @Column(name = "agent_log_size", nullable = false)
    private int agentLogSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public IssueReport(UUID userId, String description, String agentVersion,
                       byte[] saveGame, String saveGameName, byte[] agentLog, String agentLogName) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.description = description;
        this.agentVersion = agentVersion;
        this.saveGame = saveGame;
        this.saveGameName = saveGameName;
        this.saveGameSize = saveGame == null ? 0 : saveGame.length;
        this.agentLog = agentLog;
        this.agentLogName = agentLogName;
        this.agentLogSize = agentLog == null ? 0 : agentLog.length;
        this.createdAt = LocalDateTime.now();
    }
}
