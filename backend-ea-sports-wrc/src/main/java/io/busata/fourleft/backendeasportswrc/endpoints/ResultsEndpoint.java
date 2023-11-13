package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubResultsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubStandingsMessageFactory;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResultsService;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResultsEndpoint {

    private final ClubResultsService clubResultsService;
    private final ClubResultsMessageFactory clubResultsMessageFactory;
    private final ClubStandingsMessageFactory standingsMessageFactory;


    @GetMapping("/api/results/{clubId}/current")
    String getCurrentResults(@PathVariable String clubId) {
        return clubResultsService.getCurrentResults(clubId).map(clubResultsMessageFactory::createResultPost).map(MessageEmbed::toData).map(DataObject::toString).orElse("");
    }

    @GetMapping("/api/results/{clubId}/previous")
    String getPreviousResults(@PathVariable String clubId) {
        return clubResultsService.getPreviousResults(clubId).map(clubResultsMessageFactory::createResultPost).map(MessageEmbed::toData).map(DataObject::toString).orElse("");
    }

    @GetMapping("/api/results/{clubId}/standings")
    String getStandings(@PathVariable String clubId) {
        List<ChampionshipStanding> standings = clubResultsService.getStandings(clubId).stream().sorted(Comparator.comparing(ChampionshipStanding::getRank)).limit(50).toList();
        return standingsMessageFactory.createStandingsPost(standings).toData().toString();
    }
}
