package io.busata.fourleft.domain.views.configuration.results_views;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResultsViewRepository extends JpaRepository<ResultsView, UUID> {
}