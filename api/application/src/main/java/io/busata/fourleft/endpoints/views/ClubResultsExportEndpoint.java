package io.busata.fourleft.endpoints.views;

import com.opencsv.CSVWriter;
import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.endpoints.club.results.service.ClubResultToFactory;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClubResultsExportEndpoint {

    private final ClubSyncService clubSyncService;
    private final ClubResultToFactory clubResultToFactory;

    @GetMapping(Routes.CLUB_RESULTS_CURRENT_EXPORT)
    public void exportCurrentAsCSV(@PathVariable Long clubId, HttpServletResponse response) throws IOException {
        final var club = clubSyncService.getOrCreate(clubId);

        Event currentEvent = club.getCurrentEvent().orElseThrow();
        writeResults(response, currentEvent);

    }
    @GetMapping(Routes.CLUB_RESULTS_PREVIOUS_EXPORT)
    public void exportPreviousAsCSV(@PathVariable Long clubId, HttpServletResponse response) throws IOException {
        final var club = clubSyncService.getOrCreate(clubId);

        Event currentEvent = club.getPreviousEvent().orElseThrow();
        writeResults(response, currentEvent);

    }

    private void writeResults(HttpServletResponse response, Event event) throws IOException {
        final var result = clubResultToFactory.create(event);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"results.csv\"");


        try(CSVWriter writer = new CSVWriter(response.getWriter())) {

            writer.writeNext(new String[]{
                    "Rank",
                    "Name",
                    "Nationality",
                    "Vehicle",
                    "Total time",
                    "Total diff",
                    "DNF"
            });

            for (ResultEntryTo entry : result.entries()) {

                writer.writeNext(
                        new String[]{
                                entry.rank().toString(),
                                entry.name(),
                                entry.nationality(),
                                entry.vehicle(),
                                entry.totalTime(),
                                entry.totalDiff(),
                                entry.isDnf().toString()
                        }
                );

            }
        }
    }
}
