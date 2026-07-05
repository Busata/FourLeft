package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.infrastructure.properties.TimeTrialExportProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * The raw-data CSV export of a time-trial board: one file per combination under
 * {@code time-trial-export.output-directory}, holding the board's current snapshot. Regenerated
 * after every fetch (via the {@code TT_BOARD_FETCHED} RabbitMQ event → {@code TT_EXPORT} job) so
 * the file tracks the stored data; also built on demand when a download hits a board that has no
 * file yet. Mirrors {@link io.busata.fourleft.backendeasportswrc.domain.services.clubExport.ClubExportService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrialExportService {

    private final TimeTrialLeaderboardEntryRepository entryRepository;
    private final TimeTrialExportProperties properties;

    /**
     * Rebuild and write one board's CSV from its current snapshot. Returns the number of entry rows
     * written (a board whose latest fetch found nothing yields a header-only file, keeping the file
     * in step with the emptied board rather than serving stale data).
     */
    @Transactional(readOnly = true)
    public int exportBoard(String combinationId) {
        List<TimeTrialLeaderboardEntry> entries = entryRepository.findLatestByCombinationId(combinationId);
        String csv = buildCsv(entries);

        Path outputPath = getOutputPath(combinationId);
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, csv, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV export for board " + combinationId, e);
        }
        log.info("Exported {} entries of board {} to {}", entries.size(), combinationId, outputPath);
        return entries.size();
    }

    /** The cached CSV file's content, or empty when the board was never exported. */
    public Optional<String> readCsv(String combinationId) {
        Path filePath = getOutputPath(combinationId);
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(filePath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to read cached CSV export for board {}: {}", combinationId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /** The cached CSV, generating (and caching) it first if the board has no file yet. */
    @Transactional(readOnly = true)
    public String readOrCreateCsv(String combinationId) {
        return readCsv(combinationId).orElseGet(() -> {
            exportBoard(combinationId);
            return readCsv(combinationId).orElseThrow(
                    () -> new IllegalStateException("Export produced no readable file for board " + combinationId));
        });
    }

    /**
     * Raw fields as fetched from Racenet (times stay in their {@code "mm:ss.SSS"} string form), one
     * {@code split_N} column per sector up to the board's widest entry. Quotes any field holding a
     * comma/quote/newline (display names are free text).
     */
    private String buildCsv(List<TimeTrialLeaderboardEntry> entries) {
        int maxSplits = entries.stream()
                .mapToInt(e -> e.getSplits() == null ? 0 : e.getSplits().size())
                .max().orElse(0);

        StringBuilder csv = new StringBuilder(
                "rank,display_name,nationality_id,platform,vehicle,time,difference_to_first,time_penalty,fetched_at");
        for (int i = 1; i <= maxSplits; i++) {
            csv.append(",split_").append(i);
        }
        csv.append('\n');

        for (TimeTrialLeaderboardEntry entry : entries) {
            csv.append(entry.getRank() == null ? "" : entry.getRank()).append(',')
                    .append(escape(entry.getDisplayName())).append(',')
                    .append(entry.getNationalityID() == null ? "" : entry.getNationalityID()).append(',')
                    .append(entry.getPlatform() == null ? "" : entry.getPlatform()).append(',')
                    .append(escape(entry.getVehicle())).append(',')
                    .append(escape(entry.getTime())).append(',')
                    .append(escape(entry.getDifferenceToFirst())).append(',')
                    .append(escape(entry.getTimePenalty())).append(',')
                    .append(entry.getFetchedAt() == null ? "" : entry.getFetchedAt());
            List<String> splits = entry.getSplits() == null ? List.of() : entry.getSplits();
            for (int i = 0; i < maxSplits; i++) {
                csv.append(',').append(i < splits.size() ? escape(splits.get(i)) : "");
            }
            csv.append('\n');
        }
        return csv.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private Path getOutputPath(String combinationId) {
        return Paths.get(properties.getOutputDirectory(), combinationId + ".csv");
    }
}
