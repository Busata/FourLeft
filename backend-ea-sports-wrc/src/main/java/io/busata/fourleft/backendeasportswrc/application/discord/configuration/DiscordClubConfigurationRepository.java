package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface DiscordClubConfigurationRepository extends JpaRepository<DiscordClubConfiguration, UUID> {

    @Query("select dcc from DiscordClubConfiguration dcc where dcc.clubId=:clubId")
    List<DiscordClubConfiguration> findByClubId(@Param("clubId") String clubId);

    @Query("select dcc from DiscordClubConfiguration dcc where dcc.channelId=:channelId")
    Optional<DiscordClubConfiguration> findByChannelId(@Param("channelId") Long channelId);
}