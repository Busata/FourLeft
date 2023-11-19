package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import io.busata.fourleft.common.Platform;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class ClubLeaderboardEntry {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ssid", updatable = false, insertable = false, referencedColumnName = "id")
    Profile profile;

    @Column()
    String ssid;

    String displayName;

    String wrcPlayerId;

    Long rank;
    Long rankAccumulated;
    Long nationalityID;
    Long platform;

    String vehicle;

    Duration time;
    Duration differenceToFirst;
    Duration differenceAccumulated;
    Duration timeAccumulated;
    Duration timePenalty;

    @Builder
    public ClubLeaderboardEntry(String displayName, String wrcPlayerId, String ssid, Long rank, Long rankAccumulated, Long nationalityID, Long platform, String vehicle, Duration time, Duration differenceToFirst, Duration differenceAccumulated, Duration timeAccumulated, Duration timePenalty) {
        this.displayName = displayName;
        this.wrcPlayerId = wrcPlayerId;
        this.ssid = ssid;
        this.rank = rank;
        this.rankAccumulated = rankAccumulated;
        this.nationalityID = nationalityID;
        this.platform = platform;
        this.vehicle = vehicle;
        this.time = time;
        this.differenceToFirst = differenceToFirst;
        this.differenceAccumulated = differenceAccumulated;
        this.timeAccumulated = timeAccumulated;
        this.timePenalty = timePenalty;
    }

    public String getPlayerKey() {
        return Optional.ofNullable(getSsid()).orElse(Optional.ofNullable(getWrcPlayerId()).orElse(getAlias()));
    }

    public String getAlias() {
        return Optional.ofNullable(profile).map(Profile::getDisplayName).orElse(this.displayName);
    }

    public Optional<Platform> getProfilePlatform() {
        return Optional.ofNullable(profile).map(Profile::getPlatform);
    }

    public boolean isTracked() {
        return Optional.ofNullable(profile).map(Profile::isTrackDiscord).orElse(false);
    }
}
