package io.busata.fourleftdiscord.autoposting.club_results;

public interface AutoPostableResultEntry {

    String getName();

    Long getRank();

    boolean getIsDnf();

    String getNationality();

    String getTotalTime();

    String getTotalDiff();

    long getLastStageRank();

    String getVehicleName();
}
