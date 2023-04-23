package io.busata.fourleft.endpoints.discord.configuration;

import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.api.models.configuration.DefaultPointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.FixedPointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.PointSystemTo;
import io.busata.fourleft.api.models.configuration.PointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import io.busata.fourleft.api.models.configuration.results.ConcatenationViewTo;
import io.busata.fourleft.api.models.configuration.results.MergedViewTo;
import io.busata.fourleft.api.models.configuration.results.RacenetFilterElementTo;
import io.busata.fourleft.api.models.configuration.results.PartitionViewTo;
import io.busata.fourleft.api.models.configuration.results.PlayerFilterTo;
import io.busata.fourleft.api.models.configuration.results.ResultsViewTo;
import io.busata.fourleft.api.models.configuration.results.SingleClubViewTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.DiscordChannelConfiguration;
import io.busata.fourleft.domain.configuration.player_restrictions.PlayerFilter;
import io.busata.fourleft.domain.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointSystem;
import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.ConcatenationView;
import io.busata.fourleft.domain.configuration.results_views.MergeResultsView;
import io.busata.fourleft.domain.configuration.results_views.RacenetFilterElement;
import io.busata.fourleft.domain.configuration.results_views.PartitionView;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.helpers.Factory;

import java.util.stream.Collectors;

@Factory
public class DiscordChannelConfigurationToFactory {


    public DiscordChannelConfigurationTo create(DiscordChannelConfiguration discordChannelConfiguration) {
        return new DiscordChannelConfigurationTo(
                discordChannelConfiguration.getId(),
    discordChannelConfiguration.getAutopostClubViews().size() > 0,
                discordChannelConfiguration.getChannelId(),
                createClubViewTo(discordChannelConfiguration.getCommandsClubView())
        );
    }

    private ClubViewTo createClubViewTo(ClubView clubView) {

        return new ClubViewTo(
                clubView.getId(),
                "",
                clubView.getBadgeType(),
                createResultsViewTo(clubView.getResultsView()),
                createPointsViewTo(clubView.getPointsCalculator())
        );
    }

    private ResultsViewTo createResultsViewTo(ResultsView resultsView) {
        ResultsViewTo resultsViewTo = switch (resultsView) {
            case SingleClubView view -> createSingleClubViewTo(view);
            case MergeResultsView view -> createMergeResultsViewTo(view);
            case PartitionView view -> createPartitionViewTo(view);
            case ConcatenationView view -> createConcatenationViewTo(view);
            default -> throw new UnsupportedOperationException("Unexpected value");
        };
        resultsViewTo.setId(resultsView.getId());
        resultsViewTo.setAssociatedClubs(resultsView.getAssociatedClubs());
        return resultsViewTo;

    }


    private SingleClubViewTo createSingleClubViewTo(SingleClubView view) {
        return new SingleClubViewTo(
                view.getId(),
                view.getClubId(),
                view.getName(),
                view.hasPowerStage(),
                view.getPowerStageIndices().stream().findFirst().orElse(null),
                createPlayerFilterTo(view.getPlayerFilter())
        );
    }

    private PlayerFilterTo createPlayerFilterTo(PlayerFilter playerFilter) {
        return new PlayerFilterTo(
                playerFilter.getFilterType(),
                playerFilter.getRacenetNames()
        );
    }

    private ResultsViewTo createPartitionViewTo(PartitionView view) {
        return new PartitionViewTo(
                createResultsViewTo(view.getResultsView()),
                view.getPartitionElements().stream().map(this::createPartitionElementTo).collect(Collectors.toList())
        );
    }

    private RacenetFilterElementTo createPartitionElementTo(RacenetFilterElement racenetFilterElement) {
        return new RacenetFilterElementTo(
                racenetFilterElement.getName(),
                racenetFilterElement.getRacenetNames()
        );
    }

    private ResultsViewTo createMergeResultsViewTo(MergeResultsView view) {
        return new MergedViewTo(
                view.getName(),
                createPlayerFilterTo(view.getPlayerFilter()),
                view.getResultViews().stream().map(this::createSingleClubViewTo).collect(Collectors.toList())
        );
    }


    private ResultsViewTo createConcatenationViewTo(ConcatenationView view) {
        return new ConcatenationViewTo(
                view.getName(),
                view.getResultViews().stream().map(this::createSingleClubViewTo).collect(Collectors.toList())
        );
    }


    private PointsCalculatorTo createPointsViewTo(PointsCalculator pointsCalculator) {

        return switch(pointsCalculator) {
            case DefaultPointsCalculator $ -> new DefaultPointsCalculatorTo();
            case FixedPointsCalculator fixedPointsCalculator -> createFixedPointsTo(fixedPointsCalculator);
            default -> throw new IllegalStateException("Unexpected value: ");
        };
    }

    private PointsCalculatorTo createFixedPointsTo(FixedPointsCalculator fixedPointsCalculator) {
        return new FixedPointsCalculatorTo(
                fixedPointsCalculator.getJoinChampionshipsCount(),
                fixedPointsCalculator.getOffsetChampionship(),
                createPointSystemTo(fixedPointsCalculator.getPointSystem())
        );
    }

    private PointSystemTo createPointSystemTo(PointSystem pointSystem) {
        return new PointSystemTo(
                pointSystem.getId(),
                pointSystem.getDescription(),
                pointSystem.getDefaultRankingPoint(),
                pointSystem.getDefaultPowerstagePoint(),
                0,
                pointSystem.getRankingPoints(),
                pointSystem.getPowerStagePoints()
        );
    }


}
