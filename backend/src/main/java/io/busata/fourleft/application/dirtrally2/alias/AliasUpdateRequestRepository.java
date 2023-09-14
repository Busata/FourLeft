package io.busata.fourleft.application.dirtrally2.alias;

import io.busata.fourleft.domain.dirtrally2.alias.AliasUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface AliasUpdateRequestRepository extends JpaRepository<AliasUpdateRequest, UUID> {


    boolean existsByIdAndRequestedAlias(UUID id, String requestedAlias);
}