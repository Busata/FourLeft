package io.busata.fourleft.backendeasportswrc.domain.events;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;


public record AutoPostEditMessageEvent(Long channelId, Long messageId, AutoPostMessageSummary summary) {
}
