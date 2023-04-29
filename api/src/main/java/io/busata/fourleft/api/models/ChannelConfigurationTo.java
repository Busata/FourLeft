package io.busata.fourleft.api.models;


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
