package io.busata.fourleft.domain.configuration.repository;

import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SingleClubViewRepository extends JpaRepository<SingleClubView, UUID> {
}