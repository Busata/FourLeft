package io.busata.fourleft.backendeasportswrc.domain.services.clubExport;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubExportConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubExportConfigurationService {

    private final ClubExportConfigurationRepository clubExportConfigurationRepository;

    @Transactional(readOnly = true)
    public List<ClubExportConfiguration> findAllExportConfigurations() {
        return clubExportConfigurationRepository.findAll();
    }

    @Transactional
    public ClubExportConfiguration addClubToExport(String clubId) {
        Optional<ClubExportConfiguration> existing = clubExportConfigurationRepository.findByClubId(clubId);

        if (existing.isPresent()) {
            log.info("Club {} already configured for export", clubId);
            return existing.get();
        }

        ClubExportConfiguration config = new ClubExportConfiguration(clubId);
        clubExportConfigurationRepository.save(config);
        log.info("Added club {} to export list", clubId);
        return config;
    }

    @Transactional
    public void removeClubFromExport(String clubId) {
        Optional<ClubExportConfiguration> config = clubExportConfigurationRepository.findByClubId(clubId);

        if (config.isPresent()) {
            clubExportConfigurationRepository.delete(config.get());
            log.info("Removed club {} from export list", clubId);
        } else {
            log.warn("Club {} not found in export configurations", clubId);
        }
    }

    @Transactional
    public void setClubExportEnabled(String clubId, boolean enabled) {
        Optional<ClubExportConfiguration> config = clubExportConfigurationRepository.findByClubId(clubId);

        if (config.isPresent()) {
            config.get().setEnabled(enabled);
            clubExportConfigurationRepository.save(config.get());
            log.info("Set club {} export enabled to {}", clubId, enabled);
        } else {
            log.warn("Club {} not found in export configurations", clubId);
        }
    }

    @Transactional
    public void setMaxChampionships(String clubId, Integer maxChampionships) {
        Optional<ClubExportConfiguration> config = clubExportConfigurationRepository.findByClubId(clubId);

        if (config.isPresent()) {
            config.get().setMaxChampionships(maxChampionships);
            clubExportConfigurationRepository.save(config.get());
            log.info("Set club {} max championships to {}", clubId, maxChampionships);
        } else {
            log.warn("Club {} not found in export configurations", clubId);
        }
    }
}
