package io.busata.fourleft.domain.discord.repository;

import io.busata.fourleft.domain.discord.models.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FieldMappingRepository extends JpaRepository<FieldMapping, UUID> {

}
