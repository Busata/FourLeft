package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "championship_standing")
public class ChampionshipStanding {

    @Id
    @GeneratedValue
    UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    private Championship championship;

    @ManyToOne(fetch = FetchType.EAGER)
    @Setter
    @JoinColumn(name = "ssid", updatable = false, insertable = false, referencedColumnName = "id")
    Profile profile;

    @Column()
    String ssid;


    String displayName;

    Integer pointsAccumulated;
    Integer pointsAccumulatedPrevious;

    Integer rank;
    Integer previousRank;


    Integer nationalityId;

    public ChampionshipStanding(UUID id, String ssid, String displayName, Integer pointsAccumulated, Integer rank, Integer nationalityId) {
        this.ssid = ssid;
        this.displayName = displayName;
        this.pointsAccumulated = pointsAccumulated;
        this.rank = rank;
        this.nationalityId = nationalityId;


        this.previousRank = rank;
        this.pointsAccumulatedPrevious = pointsAccumulated;
    }

    public void updatePoints(int pointsAccumulated) {
        this.pointsAccumulatedPrevious = this.pointsAccumulated;
        this.pointsAccumulated = pointsAccumulated;
    }

    public void updateRank(int rank) {
        this.previousRank = this.rank;
        this.rank = rank;
    }
    
    public void updateStandings(int rank, int pointsAccumulated) {
        this.previousRank = this.rank;
        this.rank = rank;

        this.pointsAccumulatedPrevious = this.pointsAccumulated;
        this.pointsAccumulated = pointsAccumulated;
    }

    public int getPointsDifference() {
        return this.pointsAccumulated - this.pointsAccumulatedPrevious;
    }
    public int getRankDifference() {
        return this.previousRank - this.rank;
    }

    public boolean isTracked() {
        return Optional.ofNullable(profile).map(Profile::isTrackDiscord).orElse(false);
    }

    public String getDisplayName() {
        return Optional.ofNullable(profile).map(Profile::getDisplayName).orElse(this.displayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ChampionshipStanding that = (ChampionshipStanding) o;

        return new EqualsBuilder().append(championship, that.championship).append(ssid, that.ssid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(championship).append(ssid).toHashCode();
    }
}