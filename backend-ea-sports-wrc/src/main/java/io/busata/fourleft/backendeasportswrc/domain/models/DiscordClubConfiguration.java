package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.backendeasportswrc.application.discord.messages.AutoPostMessageService;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubResultsMessageFactory;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
import io.busata.fourleft.common.ChannelClubMode;
import io.busata.fourleft.common.ScoringStrategy;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class DiscordClubConfiguration {

    @GeneratedValue
    @Id
    UUID id;

    // Legacy single-club column, mirrored from the primary club so a rollback to the pre-multi-club build
    // still finds it. Read clubs through getClubs()/getPrimaryClubId().
    @Getter(AccessLevel.NONE)
    String clubId;

    // Ordered; index 0 is the primary club. Eager: tiny, and read outside transactions by event listeners.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "discord_club_configuration_club", joinColumns = @JoinColumn(name = "configuration_id"))
    @OrderColumn(name = "position")
    List<ChannelClub> clubs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    ChannelClubMode mode;

    Long guildId;
    Long channelId;

    boolean enabled;

    boolean requiresTracking;

    boolean autopostingEnabled;

    boolean customScoringEnabled;

    // When true, a new-event post is followed by the time-trial top 10 (target times) for the event's board.
    boolean timeTrialTopEnabled;

    // When true, the time-trial top only lists discord-tracked players (mirrors requiresTracking for results).
    boolean timeTrialTopTrackedOnly;

    @Enumerated(EnumType.STRING)
    ScoringStrategy scoringStrategy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Map<String, Integer> scoringTable;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    ScoringAnchors scoringAnchors;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<EventRestriction> eventRestrictions;


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

    public void setTimeTrialTopEnabled(boolean timeTrialTopEnabled) {
        this.timeTrialTopEnabled = timeTrialTopEnabled;
    }

    public void setTimeTrialTopTrackedOnly(boolean timeTrialTopTrackedOnly) {
        this.timeTrialTopTrackedOnly = timeTrialTopTrackedOnly;
    }

    public void setScoringStrategy(ScoringStrategy scoringStrategy) {
        this.scoringStrategy = scoringStrategy;
    }

    public void setScoringTable(Map<String, Integer> scoringTable) {
        this.scoringTable = scoringTable;
    }

    public void setScoringAnchors(ScoringAnchors scoringAnchors) {
        this.scoringAnchors = scoringAnchors;
    }

    public void setEventRestrictions(List<EventRestriction> eventRestrictions) {
        this.eventRestrictions = eventRestrictions;
    }

    public void setMode(ChannelClubMode mode) {
        this.mode = mode;
    }

    /** The club every channel-scoped view reads while only {@link ChannelClubMode#SINGLE} has rendering. */
    public String getPrimaryClubId() {
        return clubs.isEmpty() ? null : clubs.get(0).getClubId();
    }

    public List<String> getClubIds() {
        return clubs.stream().map(ChannelClub::getClubId).toList();
    }

    public boolean tracksClub(String clubId) {
        return clubs.stream().anyMatch(club -> Objects.equals(club.getClubId(), clubId));
    }

    /** Appends the club, or relabels it when already tracked. Returns true when it was newly added. */
    public boolean addClub(String clubId, String label) {
        for (int i = 0; i < clubs.size(); i++) {
            if (Objects.equals(clubs.get(i).getClubId(), clubId)) {
                clubs.set(i, new ChannelClub(clubId, label));
                return false;
            }
        }
        clubs.add(new ChannelClub(clubId, label));
        this.clubId = getPrimaryClubId();
        return true;
    }

    /** Returns true when the club was tracked. Removing the primary promotes the next club. */
    public boolean removeClub(String clubId) {
        boolean removed = clubs.removeIf(club -> Objects.equals(club.getClubId(), clubId));
        this.clubId = getPrimaryClubId();
        return removed;
    }

    public List<EventRestriction> getEventRestrictionsOrEmpty() {
        return eventRestrictions == null ? List.of() : eventRestrictions;
    }


    public DiscordClubConfiguration(Long guildId, Long channelId, String clubId, boolean autopostingEnabled) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.mode = ChannelClubMode.SINGLE;
        addClub(clubId, null);
        this.enabled = true;
        this.autopostingEnabled = autopostingEnabled;
        this.requiresTracking = false;
        this.customScoringEnabled = false;
        this.timeTrialTopEnabled = false;
        this.timeTrialTopTrackedOnly = false;
        this.scoringStrategy = ScoringStrategy.LOOKUP_TABLE;
        this.scoringTable = new HashMap<>();
        this.autoPostTemplate = AutoPostMessageService.defaultTemplate;
        this.resultsEntryTemplate = ClubResultsMessageFactory.defaultTemplate;

    }
}
