package io.busata.fourleft.backendacrally.domain.services.agent;

import io.busata.fourleft.backendacrally.domain.models.agent.DevicePairing;
import io.busata.fourleft.backendacrally.domain.models.agent.PairingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DevicePairingRepository extends JpaRepository<DevicePairing, UUID> {

    Optional<DevicePairing> findByDeviceCodeHash(String deviceCodeHash);

    Optional<DevicePairing> findByUserCodeAndStatus(String userCode, PairingStatus status);

    boolean existsByUserCodeAndStatus(String userCode, PairingStatus status);
}
