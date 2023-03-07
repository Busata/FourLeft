package io.busata.fourleft.api.models.views;

import io.busata.fourleft.api.models.DriverEntryTo;

import java.util.List;

public record ResultListTo (
        String name,
        List<ActivityInfoTo> activityInfoTo,
        int totalUniqueEntries,
        List<DriverEntryTo> results) {
}


/*
Single
-> total time of the events
-> optional powerstage(S) total time
-> vehicle restrictions
-> player restrictions (include, exclude)
 */

/*
Concatenate
-> basically the same as a single club, but grouped together. Each tier can have its own club, powerstage, vehicle and player restrictions?
 */

/*
Merging (First concatenate, then merge the final lists into one)

 */