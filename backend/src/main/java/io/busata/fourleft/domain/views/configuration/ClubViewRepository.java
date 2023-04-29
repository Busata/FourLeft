package io.busata.fourleft.domain.views.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClubViewRepository extends JpaRepository<ClubView, UUID> {
}