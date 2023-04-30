package io.busata.fourleft.domain.discord.bot.models;

import io.busata.fourleft.common.ChampionshipPointsType;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Getter
@Table(name="channel_configuration")
public class ChannelConfiguration {

    @Id
    @GeneratedValue
    UUID id;

    String description;

    @Column(name="channel_id")
    Long channelId;

    @Column(name="club_id")
    Long clubId;

    boolean postClubResults;
    boolean postCommunityResults;
    boolean useBadges;
    boolean hasPowerStage;

    @Enumerated(EnumType.STRING)
    ChampionshipPointsType championshipPointsType;
    Integer customChampionshipCycle;

}
