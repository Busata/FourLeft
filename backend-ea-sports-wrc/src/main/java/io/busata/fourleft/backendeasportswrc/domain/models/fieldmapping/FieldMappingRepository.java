package io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FieldMappingRepository extends JpaRepository<FieldMapping, UUID> {


    @Query("Select f from FieldMapping f where f.type=:type and f.name=:name and f.context=:context")
    Optional<FieldMapping> findFieldMapping(
            @Param("type") FieldMappingType type,
            @Param("name") String name,
            @Param("context")FieldMappingContext context);
}