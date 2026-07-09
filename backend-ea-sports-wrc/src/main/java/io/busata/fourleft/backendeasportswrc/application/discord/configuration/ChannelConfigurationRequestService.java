package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationCreateTo;
import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationUpdateTo;
import io.busata.fourleft.api.easportswrc.models.EventRestrictionTo;
import io.busata.fourleft.api.easportswrc.models.RestrictionTargetsTo;
import io.busata.fourleft.api.easportswrc.models.ScoringAnchorEntryTo;
import io.busata.fourleft.api.easportswrc.models.ScoringAnchorsTo;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.configuration.ChannelConfigurationRequest;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchorEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelConfigurationRequestService {

    private final ChannelConfigurationRequestRepository requestRepository;
    private final DiscordClubConfigurationService clubConfigurationService;
    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;

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
                    toDomain(form.scoringAnchors()),
                    toDomain(form.eventRestrictions()));

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
                        toDto(config.getScoringAnchors()),
                        toDto(config.getEventRestrictionsOrEmpty())
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

    private static List<EventRestriction> toDomain(List<EventRestrictionTo> restrictions) {
        if (restrictions == null) {
            return null;
        }
        return restrictions.stream()
                .map(r -> new EventRestriction(r.type(), r.championshipId(), r.eventId(), r.displayMode(),
                        r.scoringMode(), r.penaltyPoints(), r.allowedVehicles()))
                .toList();
    }

    private static List<EventRestrictionTo> toDto(List<EventRestriction> restrictions) {
        if (restrictions == null) {
            return null;
        }
        return restrictions.stream()
                .map(r -> new EventRestrictionTo(r.type(), r.championshipId(), r.eventId(), r.displayMode(),
                        r.scoringMode(), r.penaltyPoints(), r.allowedVehicles()))
                .toList();
    }

    /**
     * The championships/events of the channel's club that a restriction rule can target. Only open
     * championships qualify — there's no point restricting a finished one — so this usually offers
     * one championship, or none.
     */
    @Transactional(readOnly = true)
    public Optional<RestrictionTargetsTo> getRestrictionTargets(UUID requestId) {
        return findClub(requestId).map(club -> new RestrictionTargetsTo(
                club.getChampionships().stream()
                        .filter(Championship::isActiveSnapshot)
                        .map(championship -> new RestrictionTargetsTo.RestrictionTargetChampionshipTo(
                                championship.getId(),
                                championship.getSettings().getName(),
                                championship.getAbsoluteOpenDate(),
                                championship.getAbsoluteCloseDate(),
                                championship.getEvents().stream()
                                        .map(event -> new RestrictionTargetsTo.RestrictionTargetEventTo(
                                                event.getId(),
                                                event.getEventSettings().getLocation(),
                                                event.getEventSettings().getVehicleClass(),
                                                event.getAbsoluteCloseDate()))
                                        .toList()))
                        .toList()));
    }

    /**
     * Distinct vehicles seen on the club's leaderboards, for the allowlist picker — optionally scoped
     * to one championship or one event.
     */
    @Transactional(readOnly = true)
    public Optional<List<String>> getVehicles(UUID requestId, String championshipId, String eventId) {
        return findClub(requestId).map(club -> {
            List<String> leaderboardIds = club.getChampionships().stream()
                    .filter(championship -> championshipId == null || Objects.equals(championship.getId(), championshipId))
                    .flatMap(championship -> championship.getEvents().stream())
                    .filter(event -> eventId == null || Objects.equals(event.getId(), eventId))
                    .filter(event -> !event.getStages().isEmpty())
                    .map(event -> event.getLastStage().getLeaderboardId())
                    .toList();

            return clubLeaderboardService.findDistinctVehicles(leaderboardIds);
        });
    }

    private Optional<Club> findClub(UUID requestId) {
        return requestRepository.findById(requestId)
                .flatMap(request -> clubConfigurationService.findByChannelId(request.getChannelId()))
                .map(DiscordClubConfiguration::getClubId)
                .map(clubService::findById);
    }
}
