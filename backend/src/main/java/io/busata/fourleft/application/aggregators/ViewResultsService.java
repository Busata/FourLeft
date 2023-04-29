package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.aggregators.ClubEventSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViewResultsService {
    private final ViewResultToFactory viewResultToFactory;

    public Optional<ViewResultTo> getCurrentResults(UUID id) {
        return viewResultToFactory.createViewResult(id, ClubEventSupplier.CURRENT);
    }
    public Optional<ViewResultTo> getPreviousResults(UUID id) {
        return viewResultToFactory.createViewResult(id, ClubEventSupplier.PREVIOUS);
    }
}
