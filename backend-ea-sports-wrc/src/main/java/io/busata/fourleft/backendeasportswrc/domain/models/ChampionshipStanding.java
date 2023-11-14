package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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