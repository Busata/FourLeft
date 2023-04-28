package io.busata.fourleft.domain.discord.bot.repository;

import io.busata.fourleft.domain.discord.bot.models.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FieldMappingRepository extends JpaRepository<FieldMapping, UUID> {

}
