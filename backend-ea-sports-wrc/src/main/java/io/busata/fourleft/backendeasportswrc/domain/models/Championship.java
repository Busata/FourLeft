package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor
public class Championship {

    @Id
    String id;

    @Embedded
    ChampionshipSettings settings;


    ZonedDateTime absoluteOpenDate;
    ZonedDateTime absoluteCloseDate;

    @Enumerated(EnumType.STRING)
    EventStatus status;

    @ManyToOne
    @Setter
    Club club;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("absoluteCloseDate asc")
    @JoinColumn(name = "championship_id")
    List<Event> events = new ArrayList<>();


    @OneToMany(mappedBy = "championship", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ChampionshipStanding> standings = new ArrayList<>();



    @Setter
    boolean updatedAfterFinish = false;


    public Championship(String id, ChampionshipSettings settings, ZonedDateTime absoluteOpenDate, ZonedDateTime absoluteCloseDate) {
        this.id = id;
        this.settings = settings;
        this.absoluteOpenDate = absoluteOpenDate;
        this.absoluteCloseDate = absoluteCloseDate;

        this.updateStatus();
    }

    public void updateStatus() {
        var now = ApplicationClock.now();

        if(now.isBefore(absoluteOpenDate.toLocalDateTime())) {
            this.status = EventStatus.NOT_STARTED;
            return;
        }

        if(now.isAfter(absoluteCloseDate.toLocalDateTime())) {
            this.status = EventStatus.FINISHED;
            return;
        }

        this.status = EventStatus.OPEN;


        this.events.forEach(Event::updateStatus);
    }

    public void markClosed() {
        this.status = EventStatus.FINISHED;
        this.updatedAfterFinish = true;

        this.events.forEach(Event::markClosed);
    }

    public void updateEvents(List<Event> events) {
        this.events.clear();
        this.events.addAll(events);
    }

    public void update(List<ChampionshipStanding> updatedStandings) {
        updatedStandings.forEach(updatedStanding -> {
            var index = this.standings.indexOf(updatedStanding);

            if(index == -1) {
                updatedStanding.setChampionship(this);
                this.standings.add(updatedStanding);
            } else {
                this.standings.get(index).updateStandings(updatedStanding.rank, updatedStanding.pointsAccumulated);
            }
        });

    }

    public boolean isActiveNow() {
        var now = ApplicationClock.now().plusMinutes(1); //Update buffer

        return now.isAfter(absoluteOpenDate.toLocalDateTime()) && now.isBefore(absoluteCloseDate.toLocalDateTime());
    }


    public Optional<Event> getActiveEventSnapshot() {
        return this.getEvents().stream().filter(Event::isActiveSnapshot).findFirst();
    }

    public boolean isActiveSnapshot() {
        return this.status == EventStatus.OPEN;
    }
    public boolean isUpcomingSnapshot() {
        return this.status == EventStatus.NOT_STARTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Championship that = (Championship) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    public boolean hasFinishedEvent() {
        return this.events.stream().anyMatch(event -> event.getStatus() == EventStatus.FINISHED);
    }
}
