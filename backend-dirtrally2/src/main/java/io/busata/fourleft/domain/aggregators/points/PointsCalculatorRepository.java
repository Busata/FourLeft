package io.busata.fourleft.domain.aggregators.points;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PointsCalculatorRepository extends JpaRepository<PointsCalculator, UUID> {
}