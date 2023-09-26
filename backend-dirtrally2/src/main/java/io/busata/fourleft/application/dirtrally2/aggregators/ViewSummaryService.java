package io.busata.fourleft.application.dirtrally2.aggregators;

import io.busata.fourleft.api.models.views.ViewEventSummaryTo;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViewSummaryService {


    private final ClubViewRepository repository;
    private final ViewEventSummaryToFactory viewEventSummaryToFactory;

    public ViewEventSummaryTo getEventSummary(UUID viewId) {
        final var clubView = repository.findById(viewId).orElseThrow();
        return viewEventSummaryToFactory.create(clubView.getResultsView());

    }
}
