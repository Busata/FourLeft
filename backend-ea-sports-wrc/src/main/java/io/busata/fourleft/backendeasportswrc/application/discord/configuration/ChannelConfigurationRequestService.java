package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationCreateTo;
import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationUpdateTo;
import io.busata.fourleft.api.easportswrc.models.ScoringAnchorEntryTo;
import io.busata.fourleft.api.easportswrc.models.ScoringAnchorsTo;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.configuration.ChannelConfigurationRequest;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchorEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelConfigurationRequestService {

    private final ChannelConfigurationRequestRepository requestRepository;
    private final DiscordClubConfigurationService clubConfigurationService;

    @Transactional
    public UUID requestConfiguration(Long guildId, Long channelId, String discordId) {
        ChannelConfigurationRequest request = requestRepository.save(
                new ChannelConfigurationRequest(guildId, channelId, discordId));

        return request.getId();
    }

    @Transactional(readOnly = true)
    public Optional<ChannelConfigurationTo> getConfiguration(UUID requestId) {
        return requestRepository.findById(requestId).map(this::toConfigurationTo);
    }

    @Transactional
    public Optional<ChannelConfigurationTo> createConfiguration(UUID requestId, ChannelConfigurationCreateTo form) {
        return requestRepository.findById(requestId).map(request -> {
            if (clubConfigurationService.findByChannelId(request.getChannelId()).isEmpty()) {
                clubConfigurationService.createConfiguration(
                        request.getGuildId(),
                        request.getChannelId(),
                        form.clubId(),
                        form.autopostingEnabled(),
                        form.requiresTracking());
            }

            return toConfigurationTo(request);
        });
    }

    @Transactional
    public Optional<ChannelConfigurationTo> updateConfiguration(UUID requestId, ChannelConfigurationUpdateTo form) {
        return requestRepository.findById(requestId).map(request -> {
            clubConfigurationService.updateConfiguration(
                    request.getChannelId(),
                    form.autopostingEnabled(),
                    form.requiresTracking(),
                    form.customScoringEnabled(),
                    form.scoringStrategy(),
                    form.scoringTable(),
                    toDomain(form.scoringAnchors()));

            return toConfigurationTo(request);
        });
    }

    @Transactional
    public Optional<ChannelConfigurationTo> removeConfiguration(UUID requestId) {
        return requestRepository.findById(requestId).map(request -> {
            clubConfigurationService.removeConfiguration(request.getChannelId());

            return toConfigurationTo(request);
        });
    }

    private ChannelConfigurationTo toConfigurationTo(ChannelConfigurationRequest request) {
        Optional<DiscordClubConfiguration> configuration = clubConfigurationService.findByChannelId(request.getChannelId());

        return configuration
                .map(config -> new ChannelConfigurationTo(
                        String.valueOf(request.getGuildId()),
                        String.valueOf(request.getChannelId()),
                        true,
                        config.getClubId(),
                        config.isAutopostingEnabled(),
                        config.isRequiresTracking(),
                        config.isEnabled(),
                        config.isCustomScoringEnabled(),
                        config.getScoringStrategy(),
                        config.getScoringTable(),
                        toDto(config.getScoringAnchors())
                ))
                .orElseGet(() -> new ChannelConfigurationTo(
                        String.valueOf(request.getGuildId()),
                        String.valueOf(request.getChannelId()),
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ));
    }

    private static ScoringAnchors toDomain(ScoringAnchorsTo dto) {
        if (dto == null) {
            return null;
        }
        List<ScoringAnchorEntry> entries = dto.entries() == null ? List.of() : dto.entries().stream()
                .map(e -> new ScoringAnchorEntry(e.position(), e.points(), e.decrease()))
                .toList();
        return new ScoringAnchors(dto.floor(), entries);
    }

    private static ScoringAnchorsTo toDto(ScoringAnchors anchors) {
        if (anchors == null) {
            return null;
        }
        List<ScoringAnchorEntryTo> entries = anchors.entries() == null ? List.of() : anchors.entries().stream()
                .map(e -> new ScoringAnchorEntryTo(e.position(), e.points(), e.decrease()))
                .toList();
        return new ScoringAnchorsTo(anchors.floor(), entries);
    }
}
