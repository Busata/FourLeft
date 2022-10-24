package io.busata.fourleft.domain.clubs.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "championship")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "championship")
public class Championship {

    @Id
    @GeneratedValue
    private UUID id;

    private String referenceId;

    private String name;

    private boolean isActive;
    private boolean hardcoreDamage;
    private boolean unexpectedMoments;
    private boolean allowAssists;
    private boolean forceCockpitCamera;


    public boolean isInActive() {
        return !isActive;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="club_id")
    private Club club;


    @OneToMany(mappedBy="championship", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("referenceId asc")
    private List<Event> events = new ArrayList<>();


    public List<Event> getEvents() {
        return this.events.stream().filter(Event::exists).toList();
    }

    @OneToMany(mappedBy="championship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StandingEntry> entries = new ArrayList<>();

    public void updateEvents(List<Event> value) {
        events.clear();
        events.addAll(value);
    }

    public long getOrder() {
        return Long.parseLong(getReferenceId());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Championship that = (Championship) o;
        return referenceId.equals(that.referenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceId);
    }

    public void updateEntries(List<StandingEntry> collect) {
        this.entries.clear();
        this.entries.addAll(collect);
    }
}
