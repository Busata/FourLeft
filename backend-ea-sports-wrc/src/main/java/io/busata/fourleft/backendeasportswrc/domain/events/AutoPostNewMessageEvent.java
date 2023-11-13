package io.busata.fourleft.backendeasportswrc.domain.events;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;

import java.util.List;

public record AutoPostNewMessageEvent(Long channelId, AutoPostMessageSummary summary) {
}
