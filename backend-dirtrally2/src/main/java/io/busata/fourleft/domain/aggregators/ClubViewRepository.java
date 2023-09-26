package io.busata.fourleft.domain.aggregators;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClubViewRepository extends JpaRepository<ClubView, UUID> {
}