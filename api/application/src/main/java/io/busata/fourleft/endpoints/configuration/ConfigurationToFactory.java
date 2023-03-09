package io.busata.fourleft.endpoints.configuration;

import io.busata.fourleft.api.models.configuration.*;
import io.busata.fourleft.domain.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointSystem;
import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.CommunityChallengeView;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TieredView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConfigurationToFactory {


    public ResultsViewTo create(ResultsView resultsView) {
        if(resultsView == null) {
            return null;
        }
        if (resultsView instanceof SingleClubView) {
            return createSingleClubViewTo((SingleClubView) resultsView);
        } else if (resultsView instanceof TieredView) {
            return createTiersViewTo((TieredView) resultsView);
        } else if (resultsView instanceof CommunityChallengeView) {
            return createCommunityChallegeViewTo((CommunityChallengeView) resultsView);
        } else {
            throw new IllegalArgumentException("Couldn't create a results view from this type");
        }
    }

    private SingleClubViewTo createSingleClubViewTo(SingleClubView resultsView) {
        return new SingleClubViewTo(resultsView.getClubId(), resultsView.getBadgeType(), new PlayerRestrictionsTo());
    }
    private TiersViewTo createTiersViewTo(TieredView resultsView) {
        return new TiersViewTo(
                resultsView.isPowerStage(),
                resultsView.getDefaultPowerstageIndex(),
                resultsView.getResultViews().stream().map(this::createSingleClubViewTo).collect(Collectors.toList()));
    }
    private CommunityChallengeViewTo createCommunityChallegeViewTo(CommunityChallengeView resultsView) {
        return new CommunityChallengeViewTo(
                resultsView.isPostDailies(),
                resultsView.isPostWeeklies(),
                resultsView.isPostMonthlies(),
                resultsView.getBadgeType()

        );
    }

    public PointsCalculatorTo create(PointsCalculator pointsCalculator) {
        if(pointsCalculator == null) {
            return null;
        }
        if (pointsCalculator instanceof DefaultPointsCalculator) {
            return createDefaultPointsCalculatorTo((DefaultPointsCalculator) pointsCalculator);
        } else if (pointsCalculator instanceof FixedPointsCalculator) {
            return createFixedPointsCalculatorTo((FixedPointsCalculator) pointsCalculator);
        } else {
            throw new IllegalArgumentException("Couldn't create a results view from this type");
        }
    }

    private FixedPointsCalculatorTo createFixedPointsCalculatorTo(FixedPointsCalculator pointsCalculator) {
        return new FixedPointsCalculatorTo(
                pointsCalculator.getJoinChampionshipsCount(),
                pointsCalculator.getOffsetChampionship(),
                createPointSystemTo(pointsCalculator.getPointSystem())
        );
    }

    private PointSystemTo createPointSystemTo(PointSystem pointSystem) {
        return new PointSystemTo(
                pointSystem.getId(),
                pointSystem.getDescription(),
                pointSystem.getRankingPoints(),
                pointSystem.getPowerStagePoints()
        );
    }

    private DefaultPointsCalculatorTo createDefaultPointsCalculatorTo(DefaultPointsCalculator pointsCalculator) {
        return new DefaultPointsCalculatorTo();
    }
}
