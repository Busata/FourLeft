package io.busata.fourleft.api.models;

import io.busata.fourleft.domain.discord.bot.models.ChampionshipPointsType;

public record ChannelConfigurationTo(
        String description,
        Long channelId,
        Long clubId,
        boolean postClubResults,
        boolean postCommunityResults,
        boolean useBadges,
        boolean hasPowerStage,
        ChampionshipPointsType championshipPointsType,
        Integer customChampionshipCycle
        ) {

}
