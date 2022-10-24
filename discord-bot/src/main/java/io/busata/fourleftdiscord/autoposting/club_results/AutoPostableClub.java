package io.busata.fourleftdiscord.autoposting.club_results;

import java.util.List;

public interface AutoPostableClub {
    String getEventId();

    int getEntryCount();

    List<AutoPostableResultEntry> getEntries();

    String getEventChallengeId();

    String getCountry();

    String getStageName();

    String getVehicleClass();
}
