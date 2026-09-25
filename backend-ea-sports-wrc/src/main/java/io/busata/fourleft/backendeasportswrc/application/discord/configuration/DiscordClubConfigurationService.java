package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.common.ChannelClubMode;
import io.busata.fourleft.common.ScoringStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordClubConfigurationService {

    private final ClubConfigurationService clubConfigurationService;
    private final DiscordClubConfigurationRepository repository;


    @Transactional(readOnly = true)
    public List<DiscordClubConfiguration> findByClubId(String clubId) {
        return this.repository.findByClubId(clubId);
    }

    @Transactional(readOnly = true)
    public Optional<DiscordClubConfiguration> findByChannelId(Long channelId) {
        return this.repository.findByChannelId(channelId);
    }

    @Transactional(readOnly = true)
    public List<DiscordClubConfiguration> getConfigurations() {
        return this.repository.findAll();
    }

    @Transactional
    public void createConfiguration(Long guildId, Long channelID, String clubId, boolean autoPostingEnabled) {
        createConfiguration(guildId, channelID, clubId, autoPostingEnabled, false);
    }

    @Transactional
    public DiscordClubConfiguration createConfiguration(Long guildId, Long channelID, String clubId, boolean autoPostingEnabled, boolean requiresTracking) {
        // A channel holds one configuration; a second club for it is added to that one instead of creating
        // a duplicate row (which findByChannelId could not resolve).
        Optional<DiscordClubConfiguration> existing = this.repository.findByChannelId(channelID);
        if (existing.isPresent()) {
            log.warn("Channel {} is already configured; adding club {} to it instead of creating a new configuration", channelID, clubId);
            return addClub(existing.get(), clubId, null);
        }

        this.clubConfigurationService.addClubSync(clubId);

        DiscordClubConfiguration configuration = new DiscordClubConfiguration(
                guildId,
                channelID,
                clubId,
                autoPostingEnabled
        );
        configuration.setRequiresTracking(requiresTracking);

        return this.repository.save(configuration);
    }

    @Transactional
    public Optional<DiscordClubConfiguration> updateConfiguration(Long channelId, boolean autopostingEnabled, boolean requiresTracking,
                                                                  boolean customScoringEnabled, boolean timeTrialTopEnabled,
                                                                  boolean timeTrialTopTrackedOnly,
                                                                  ScoringStrategy scoringStrategy,
                                                                  Map<String, Integer> scoringTable, ScoringAnchors scoringAnchors,
                                                                  List<EventRestriction> eventRestrictions) {
        return this.repository.findByChannelId(channelId).map(configuration -> {
            configuration.setAutopostingEnabled(autopostingEnabled);
            configuration.setRequiresTracking(requiresTracking);
            configuration.setCustomScoringEnabled(customScoringEnabled);
            configuration.setTimeTrialTopEnabled(timeTrialTopEnabled);
            configuration.setTimeTrialTopTrackedOnly(timeTrialTopTrackedOnly);
            configuration.setScoringStrategy(scoringStrategy);
            configuration.setScoringTable(scoringTable);
            configuration.setScoringAnchors(scoringAnchors);
            configuration.setEventRestrictions(eventRestrictions);
            return this.repository.save(configuration);
        });
    }

    @Transactional
    public Optional<DiscordClubConfiguration> addClub(Long channelId, String clubId, String label) {
        return this.repository.findByChannelId(channelId).map(configuration -> addClub(configuration, clubId, label));
    }

    private DiscordClubConfiguration addClub(DiscordClubConfiguration configuration, String clubId, String label) {
        if (configuration.addClub(clubId, label)) {
            this.clubConfigurationService.addClubSync(clubId);
        }
        return this.repository.save(configuration);
    }

    @Transactional
    public Optional<DiscordClubConfiguration> updateMode(Long channelId, ChannelClubMode mode) {
        return this.repository.findByChannelId(channelId).map(configuration -> {
            configuration.setMode(mode);
            return this.repository.save(configuration);
        });
    }

    /**
     * Drops the club from the channel; the configuration itself goes once its last club is removed. The
     * club's sync is left running — other channels may still track it, same as before multi-club.
     */
    @Transactional
    public void removeConfiguration(Long channelId, String clubId) {
        this.repository.findByChannelId(channelId).ifPresent(configuration -> {
            if (!configuration.removeClub(clubId)) {
                return;
            }
            if (configuration.getClubs().isEmpty()) {
                this.repository.delete(configuration);
            } else {
                this.repository.save(configuration);
            }
        });
    }

    @Transactional
    public void removeConfiguration(Long channelId) {
        this.repository.removeByChannelId(channelId);
    }
}
