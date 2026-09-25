package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface DiscordClubConfigurationRepository extends JpaRepository<DiscordClubConfiguration, UUID> {

    // Any channel tracking the club, primary or not — club-driven fan-out (autoposts, event ended, ...) reaches every one.
    @Query("select distinct dcc from DiscordClubConfiguration dcc join dcc.clubs club where club.clubId=:clubId")
    List<DiscordClubConfiguration> findByClubId(@Param("clubId") String clubId);

    @Query("select dcc from DiscordClubConfiguration dcc where dcc.channelId=:channelId")
    Optional<DiscordClubConfiguration> findByChannelId(@Param("channelId") Long channelId);

    // Bulk delete; the club rows go with it through the FK's ON DELETE CASCADE.
    @Modifying
    @Query("delete from DiscordClubConfiguration dcc where dcc.channelId=:channelId")
    void removeByChannelId(@Param("channelId") Long channelId);
}
