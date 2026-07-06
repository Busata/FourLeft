package io.busata.fourleft.backendacrally.domain.services.agent;

import io.busata.fourleft.backendacrally.domain.models.agent.DevicePairing;
import io.busata.fourleft.backendacrally.domain.models.agent.PairingStatus;
import io.busata.fourleft.backendacrally.domain.models.identity.IdentityProvider;
import io.busata.fourleft.backendacrally.domain.services.identity.LinkedIdentityRepository;
import io.busata.fourleft.backendacrally.infrastructure.security.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DevicePairingService {

    private static final int EXPIRY_MINUTES = 10;

    private final DevicePairingRepository repository;
    private final ApiKeyService apiKeyService;
    private final LinkedIdentityRepository linkedIdentityRepository;

    /** Agent starts a pairing; returns the device secret + human code (raw, one time). */
    @Transactional
    public Started start(String label) {
        String deviceCode = Tokens.deviceCode();
        String userCode = uniquePendingUserCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
        repository.save(new DevicePairing(Tokens.sha256Hex(deviceCode), userCode, label, expiresAt));
        return new Started(deviceCode, userCode, expiresAt, EXPIRY_MINUTES * 60);
    }

    /** Details shown on the browser approval page for a given user code. */
    @Transactional(readOnly = true)
    public DevicePairing lookupPending(String userCode) {
        DevicePairing pairing = repository.findByUserCodeAndStatus(normalize(userCode), PairingStatus.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown or already-used code."));
        if (pairing.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "This code has expired.");
        }
        return pairing;
    }

    /** User approves the device. Requires a linked Steam — the anti-abuse gate for agent access. */
    @Transactional
    public void approve(String userCode, UUID userId) {
        if (linkedIdentityRepository.findByUserIdAndProvider(userId, IdentityProvider.STEAM).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Link your Steam account before authorizing a device.");
        }
        DevicePairing pairing = lookupPending(userCode);
        pairing.approve(userId);
    }

    @Transactional
    public void deny(String userCode) {
        repository.findByUserCodeAndStatus(normalize(userCode), PairingStatus.PENDING)
                .ifPresent(DevicePairing::deny);
    }

    /** Agent polls with its device code; on approval this mints and returns the key once. */
    @Transactional
    public PollResult poll(String deviceCode) {
        DevicePairing pairing = repository.findByDeviceCodeHash(Tokens.sha256Hex(deviceCode))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown device code."));

        return switch (pairing.getStatus()) {
            case DENIED -> new PollResult(PollStatus.DENIED, null, null);
            case CONSUMED -> new PollResult(PollStatus.CONSUMED, null, null);
            case PENDING -> pairing.isExpired()
                    ? new PollResult(PollStatus.EXPIRED, null, null)
                    : new PollResult(PollStatus.PENDING, null, null);
            case APPROVED -> {
                ApiKeyService.Issued issued = apiKeyService.issue(pairing.getUserId(), pairing.getLabel());
                pairing.consume(issued.id());
                yield new PollResult(PollStatus.APPROVED, issued.plaintext(), issued.label());
            }
        };
    }

    private String uniquePendingUserCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = Tokens.userCode();
            if (!repository.existsByUserCodeAndStatus(candidate, PairingStatus.PENDING)) {
                return candidate;
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not allocate a code, try again.");
    }

    private String normalize(String userCode) {
        return userCode == null ? "" : userCode.trim().toUpperCase();
    }

    public record Started(String deviceCode, String userCode, LocalDateTime expiresAt, long expiresInSeconds) {
    }

    public enum PollStatus { PENDING, APPROVED, DENIED, EXPIRED, CONSUMED }

    public record PollResult(PollStatus status, String apiKey, String label) {
    }
}
