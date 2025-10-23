package io.busata.fourleft.backendeasportswrc.domain.services.clubExport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.busata.fourleft.api.easportswrc.models.ClubOverviewTo;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubExportConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.services.CustomOverviewService;
import io.busata.fourleft.backendeasportswrc.infrastructure.properties.ClubExportProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubExportService {

    private final CustomOverviewService customOverviewService;
    private final ClubExportConfigurationRepository clubExportConfigurationRepository;
    private final ClubExportProperties clubExportProperties;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public void exportAllEnabledClubs() {
        List<ClubExportConfiguration> enabledClubs = clubExportConfigurationRepository.findAllEnabled();

        log.info("Starting export for {} enabled clubs", enabledClubs.size());

        for (ClubExportConfiguration config : enabledClubs) {
            try {
                exportClub(config.getClubId());
            } catch (Exception e) {
                log.error("Failed to export club {}: {}", config.getClubId(), e.getMessage(), e);
            }
        }

        log.info("Completed export for all enabled clubs");
    }

    @Transactional(readOnly = true)
    public void exportClub(String clubId) {
        log.info("Exporting club data for clubId: {}", clubId);

        try {
            ClubOverviewTo overview = customOverviewService.createOverview(clubId);

            Path outputPath = getOutputPath(clubId);
            ensureDirectoryExists(outputPath.getParent());

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), overview);

            log.info("Successfully exported club {} to {}", clubId, outputPath);
        } catch (IOException e) {
            log.error("Failed to write export file for club {}: {}", clubId, e.getMessage(), e);
            throw new RuntimeException("Failed to export club " + clubId, e);
        } catch (Exception e) {
            log.error("Failed to generate overview for club {}: {}", clubId, e.getMessage(), e);
            throw new RuntimeException("Failed to export club " + clubId, e);
        }
    }

    public Optional<ClubOverviewTo> getCachedClub(String clubId) {
        Path filePath = getOutputPath(clubId);

        if (!Files.exists(filePath)) {
            log.warn("No cached export found for club {}", clubId);
            return Optional.empty();
        }

        try {
            ClubOverviewTo overview = objectMapper.readValue(filePath.toFile(), ClubOverviewTo.class);
            log.debug("Successfully read cached export for club {}", clubId);
            return Optional.of(overview);
        } catch (IOException e) {
            log.error("Failed to read cached export for club {}: {}", clubId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Path getOutputPath(String clubId) {
        String outputDirectory = clubExportProperties.getOutputDirectory();
        return Paths.get(outputDirectory, clubId + ".json");
    }

    private void ensureDirectoryExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("Created export directory: {}", directory);
        }
    }
}
