package io.busata.fourleft.domain.configuration.event_restrictions.models;

import io.busata.fourleft.domain.options.models.Vehicle;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ViewEventRestrictions {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="results_view_id")
    ResultsView resultsView;

    String challengeId;
    String eventId;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    List<Vehicle> vehicles = new ArrayList<>();


    public boolean isValidVehicle(String vehicle) {
        return this.vehicles.isEmpty() || this.vehicles.stream().noneMatch(v -> v.matches(vehicle));
    }
}
