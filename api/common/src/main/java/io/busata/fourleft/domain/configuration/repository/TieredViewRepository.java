package io.busata.fourleft.domain.configuration.repository;

import io.busata.fourleft.domain.configuration.results_views.TieredView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TieredViewRepository extends JpaRepository<TieredView, UUID> {
}