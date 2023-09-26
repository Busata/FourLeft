package io.busata.fourleft.domain.aggregators.results;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResultsViewRepository extends JpaRepository<ResultsView, UUID> {
}