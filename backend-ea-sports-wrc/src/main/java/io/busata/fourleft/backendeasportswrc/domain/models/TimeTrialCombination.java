package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One queryable Racenet time-trial leaderboard, identified by the tuple that addresses it:
 * {@code location + route (stage) + surface condition + vehicle class}. The immutable catalog of
 * every such combination (seeded in {@code V017}). Whether a board actually exists and its entry
 * count are <em>not</em> here — those are probe observations recorded in {@code TimeTrialProbe}.
 *
 * <p>The Racenet call is
 * {@code /ea_sports_wrc/leaderboards/?selectedLocation={locationId}&selectedRoute={routeId}
 * &selectedSurfaceCondition={surfaceCondition}&selectedVehicleClass={vehicleClassId}&slot=secondary}.
 */
@Entity
@Getter
@NoArgsConstructor
public class TimeTrialCombination {

    /** {@code "{locationId}-{routeId}-{surfaceCondition}-{vehicleClassId}"}, e.g. {@code "6-99-0-19"}. */
    @Id
    private String id;

    private Long locationId;
    private String location;

    /** The stage. */
    private Long routeId;
    private String route;

    /** 0 = dry, 1 = wet. */
    private int surfaceCondition;

    private Long vehicleClassId;
    private String vehicleClass;
}
