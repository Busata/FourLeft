package io.busata.fourleft.domain.discord.bot.repository;

import io.busata.fourleft.common.FieldMappingContext;
import io.busata.fourleft.common.FieldMappingType;
import io.busata.fourleft.domain.discord.bot.models.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldMappingRepository extends JpaRepository<FieldMapping, UUID> {

    Optional<FieldMapping> findByTypeAndNameAndContext(FieldMappingType type, String name, FieldMappingContext context);

}
