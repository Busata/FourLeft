package io.busata.fourleftdiscord.autoposting.club_results.domain;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleftdiscord.autoposting.club_results.AutoPostableResultEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        return totalTime.equals(resultEntryTo.activityTotalTime()) && vehicle.equals(resultEntryTo.vehicles().get(0)) && nationality.equals(resultEntryTo.nationality());
    }

}
