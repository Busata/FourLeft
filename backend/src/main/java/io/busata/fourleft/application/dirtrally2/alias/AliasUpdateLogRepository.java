package io.busata.fourleft.application.dirtrally2.alias;

import io.busata.fourleft.domain.dirtrally2.alias.AliasUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AliasUpdateLogRepository extends JpaRepository<AliasUpdateLog, UUID> {
}