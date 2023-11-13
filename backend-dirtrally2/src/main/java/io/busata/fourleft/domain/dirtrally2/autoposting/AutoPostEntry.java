package io.busata.fourleft.domain.dirtrally2.autoposting;

import io.busata.fourleft.api.models.DriverEntryTo;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "autoPostEntry")
public class AutoPostEntry {

    @Id
    @GeneratedValue
    private UUID id;

    private String eventKey;

    String name;
    String totalTime;
    String vehicle;
    String nationality;

    Long messageId;

    public boolean hasEqualName(DriverEntryTo resultEntryTo) {
        return name.equals(resultEntryTo.racenet());
    }

    public boolean hasEqualTimeVehicleAndNationality(DriverEntryTo resultEntryTo) {
        boolean timeMatches = totalTime.equals(resultEntryTo.activityTotalTime());
        boolean nationalityMatches = nationality.equals(resultEntryTo.nationality());
        boolean vehicleMatches = vehicle.equals(resultEntryTo.vehicles().get(0).vehicleName());
        return timeMatches && vehicleMatches && nationalityMatches;
    }

}
