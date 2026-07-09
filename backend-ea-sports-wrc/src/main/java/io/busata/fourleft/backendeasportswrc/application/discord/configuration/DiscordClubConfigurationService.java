package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.common.ScoringStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                                                                  boolean customScoringEnabled, ScoringStrategy scoringStrategy,
                                                                  Map<String, Integer> scoringTable, ScoringAnchors scoringAnchors,
                                                                  List<EventRestriction> eventRestrictions) {
        return this.repository.findByChannelId(channelId).map(configuration -> {
            configuration.setAutopostingEnabled(autopostingEnabled);
            configuration.setRequiresTracking(requiresTracking);
            configuration.setCustomScoringEnabled(customScoringEnabled);
            configuration.setScoringStrategy(scoringStrategy);
            configuration.setScoringTable(scoringTable);
            configuration.setScoringAnchors(scoringAnchors);
            configuration.setEventRestrictions(eventRestrictions);
            return this.repository.save(configuration);
        });
    }

    @Transactional
    public void removeConfiguration(Long channelId, String clubId) {
        this.repository.removeByChannelAndClubId(channelId, clubId);
    }

    @Transactional
    public void removeConfiguration(Long channelId) {
        this.repository.removeByChannelId(channelId);
    }
}
