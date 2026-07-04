package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.backendeasportswrc.application.discord.messages.AutoPostMessageService;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubResultsMessageFactory;
import io.busata.fourleft.common.ScoringStrategy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class DiscordClubConfiguration {

    @GeneratedValue
    @Id
    UUID id;

    String clubId;

    Long guildId;
    Long channelId;

    boolean enabled;

    boolean requiresTracking;

    boolean autopostingEnabled;

    boolean customScoringEnabled;

    @Enumerated(EnumType.STRING)
    ScoringStrategy scoringStrategy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Map<String, Integer> scoringTable;


    String resultsEntryTemplate;
    String autoPostTemplate;


    public boolean isAutopostingDisabled() {
        return !autopostingEnabled;
    }

    public void setAutopostingEnabled(boolean autopostingEnabled) {
        this.autopostingEnabled = autopostingEnabled;
    }

    public void setRequiresTracking(boolean requiresTracking) {
        this.requiresTracking = requiresTracking;
    }

    public void setCustomScoringEnabled(boolean customScoringEnabled) {
        this.customScoringEnabled = customScoringEnabled;
    }

    public void setScoringStrategy(ScoringStrategy scoringStrategy) {
        this.scoringStrategy = scoringStrategy;
    }

    public void setScoringTable(Map<String, Integer> scoringTable) {
        this.scoringTable = scoringTable;
    }


    public DiscordClubConfiguration(Long guildId, Long channelId, String clubId, boolean autopostingEnabled) {
        this.guildId = guildId;
        this.clubId = clubId;
        this.channelId = channelId;
        this.enabled = true;
        this.autopostingEnabled = autopostingEnabled;
        this.requiresTracking = false;
        this.customScoringEnabled = false;
        this.scoringStrategy = ScoringStrategy.LOOKUP_TABLE;
        this.scoringTable = new HashMap<>();
        this.autoPostTemplate = AutoPostMessageService.defaultTemplate;
        this.resultsEntryTemplate = ClubResultsMessageFactory.defaultTemplate;

    }
}
