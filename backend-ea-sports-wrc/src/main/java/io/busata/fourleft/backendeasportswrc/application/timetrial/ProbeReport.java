package io.busata.fourleft.backendeasportswrc.application.timetrial;

/**
 * What one rally's probe pass did.
 *
 * @param probed       combinations probed (history rows written)
 * @param boardsFound  how many of those boards actually exist on Racenet
 * @param totalEntries summed entry count across the existing boards
 */
public record ProbeReport(int probed, int boardsFound, int totalEntries) {
}
