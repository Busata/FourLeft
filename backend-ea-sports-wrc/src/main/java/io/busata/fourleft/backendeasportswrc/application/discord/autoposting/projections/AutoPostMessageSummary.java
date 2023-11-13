package io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;

import java.util.List;

public record AutoPostMessageSummary(Event event, int totalEntries, List<ClubLeaderboardEntry> entries) {
}
