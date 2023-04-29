package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViewPointsService {

    private final ClubViewRepository repository;

    private final ViewPointsToFactory viewPointsToFactory;


    public ViewPointsTo getCurrentPoints(UUID viewId) {
        final var clubView = repository.findById(viewId).orElseThrow();
        return viewPointsToFactory.create(clubView);
    }
}
