package io.busata.fourleft.domain.configuration.event_restrictions.models;

import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.options.models.Vehicle;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
public class ViewEventRestrictions {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne()
    @JoinColumn(name="single_club_view_id")
    SingleClubView resultsView;

    String challengeId;
    String eventId;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    List<Vehicle> vehicles = new ArrayList<>();

    public ViewEventRestrictions(SingleClubView resultsView, String challengeId, String eventId) {
        this.resultsView = resultsView;
        this.challengeId = challengeId;
        this.eventId = eventId;
    }

    public void updateVehicles(List<Vehicle> updates) {
        vehicles.clear();
        vehicles.addAll(updates);
    }

    public boolean isValidVehicle(String vehicle) {
        return this.vehicles.isEmpty() || this.vehicles.stream().anyMatch(v -> v.matches(vehicle));
    }
}
