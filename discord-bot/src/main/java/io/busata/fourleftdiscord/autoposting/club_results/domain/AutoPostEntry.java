package io.busata.fourleftdiscord.autoposting.club_results.domain;

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

    private String eventId;
    private String challengeId;

    String name;
    String totalTime;
    String vehicle;
    String nationality;

    Long messageId;

    public boolean hasEqualName(ResultEntryTo resultEntryTo) {
        return name.equals(resultEntryTo.name());
    }

    public boolean hasEqualTimeVehicleAndNationality(ResultEntryTo resultEntryTo) {
        return totalTime.equals(resultEntryTo.totalTime()) && vehicle.equals(resultEntryTo.vehicle()) && nationality.equals(resultEntryTo.nationality());
    }

}
