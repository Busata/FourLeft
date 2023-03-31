package io.busata.fourleft.endpoints.configuration;

import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.api.models.configuration.PointSystemTo;
import io.busata.fourleft.api.models.configuration.create.CreateDiscordChannelConfigurationTo;
import io.busata.fourleft.api.models.configuration.DefaultPointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.FixedPointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.PointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.results.MergedViewTo;
import io.busata.fourleft.api.models.configuration.results.PartitionElementTo;
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
import io.busata.fourleft.domain.configuration.results_views.MergeResultsView;
import io.busata.fourleft.domain.configuration.results_views.PartitionElement;
import io.busata.fourleft.domain.configuration.results_views.PartitionView;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.helpers.Factory;

import java.util.List;

@Factory
public class DiscordChannelConfigurationFactory {


    public CreateDiscordChannelConfigurationTo create(DiscordChannelConfiguration discordChannelConfiguration) {
        return new CreateDiscordChannelConfigurationTo(
                discordChannelConfiguration.getAutopostClubViews().size() > 0,
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
        return null;
    }
    private PointsCalculatorTo createPointsViewTo(PointsCalculator pointsCalculator) {
        return null;
    }



    public DiscordChannelConfiguration create(Long channelId, CreateDiscordChannelConfigurationTo createDiscordChannelConfigurationTo) {
        ClubView clubView = createClubView(createDiscordChannelConfigurationTo.clubView());

        return new DiscordChannelConfiguration(
                null,
                channelId,
                "",
                clubView,
                List.of(clubView),
                createDiscordChannelConfigurationTo.enableAutoposts() ? List.of(clubView) : List.of()
        );

    }

    private ClubView createClubView(ClubViewTo clubView) {

        return new ClubView(
                null,
                clubView.badgeType(),
                createResultsView(clubView.resultsView()),
                createPointsView(clubView.pointsView())
        );

    }

    private ResultsView createResultsView(ResultsViewTo resultsView) {
        return switch(resultsView) {
            case (SingleClubViewTo clubViewTo) -> createSingleClubView(clubViewTo);
            case (MergedViewTo clubViewTo) -> createMergedResultsView(clubViewTo);
            case (PartitionViewTo clubViewTo) -> createPartitionView(clubViewTo);
            default -> throw new IllegalArgumentException("Unsupported");
        };
    }


    private PointsCalculator createPointsView(PointsCalculatorTo pointsView) {
        return switch(pointsView) {
            case (DefaultPointsCalculatorTo $) -> createDefaultPointsCalculator();
            case (FixedPointsCalculatorTo pointsCalculatorTo) -> createFixedPointsCalculator(pointsCalculatorTo);
            default -> throw new IllegalArgumentException("Unsupported");
        };
    }

    private PointsCalculator createFixedPointsCalculator(FixedPointsCalculatorTo pointsCalculatorTo) {
        return new FixedPointsCalculator(
                1,
                null,
                    createPointSystem(pointsCalculatorTo.getPointSystemTo())
                );
    }

    private PointSystem createPointSystem(PointSystemTo pointSystemTo) {
        return new PointSystem(
                null,
                "",
                pointSystemTo.defaultStandingPoint(),
                pointSystemTo.defaultPowerstagePoint(),
                pointSystemTo.rankingPoints(),
                pointSystemTo.powerStagePoints()
        );
    }

    private PointsCalculator createDefaultPointsCalculator() {
        return new DefaultPointsCalculator();
    }

    private PartitionView createPartitionView(PartitionViewTo clubViewTo) {
        return new PartitionView(
                createResultsView(clubViewTo.getResultsViewTo()),
                clubViewTo.getPartitionElements().stream().map(this::createPartitionElement).toList()
        );
    }

    private PartitionElement createPartitionElement(PartitionElementTo partitionElementTo) {
        return new PartitionElement(
                null,
                partitionElementTo.getName(),
                0,
                partitionElementTo.getRacenetNames()
        );
    }

    private SingleClubView createSingleClubView(SingleClubViewTo clubViewTo) {
        return new SingleClubView(
                clubViewTo.getClubId(),
                clubViewTo.getName(),
                clubViewTo.isUsePowerstage() ? List.of(clubViewTo.getPowerStageIndex()) : List.of(),
                createPlayerFilter(clubViewTo.getPlayerFilter())
        );
    }

    private PlayerFilter createPlayerFilter(PlayerFilterTo playerFilter) {
        return new PlayerFilter(
                null,
                playerFilter.getPlayerFilterType(),
                playerFilter.getRacenetNames()
        );
    }

    private MergeResultsView createMergedResultsView(MergedViewTo clubViewTo) {
        return new MergeResultsView(
                clubViewTo.getName(),
                clubViewTo.getResultViews().stream().map(this::createSingleClubView).toList()
        );
    }

}
