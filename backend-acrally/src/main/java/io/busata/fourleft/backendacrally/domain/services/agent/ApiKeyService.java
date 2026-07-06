package io.busata.fourleft.backendacrally.domain.services.agent;

import io.busata.fourleft.backendacrally.domain.models.agent.ApiKey;
import io.busata.fourleft.backendacrally.infrastructure.security.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository repository;

    /** Mints a key, persists only its hash, and hands back the one-time plaintext. */
    @Transactional
    public Issued issue(UUID userId, String label) {
        String plaintext = Tokens.apiKey();
        ApiKey key = repository.save(new ApiKey(userId, Tokens.sha256Hex(plaintext), label));
        return new Issued(key.getId(), plaintext, key.getLabel());
    }

    @Transactional(readOnly = true)
    public List<ApiKey> list(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void revoke(UUID keyId, UUID userId) {
        ApiKey key = repository.findById(keyId)
                .filter(k -> k.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such key."));
        key.revoke();
    }

    /** Resolves an active key from its plaintext (bearer token); records usage. */
    @Transactional
    public Optional<ApiKey> authenticate(String plaintext) {
        return repository.findByTokenHash(Tokens.sha256Hex(plaintext))
                .filter(ApiKey::isActive)
                .map(key -> {
                    key.markUsed();
                    return key;
                });
    }

    public record Issued(UUID id, String plaintext, String label) {
    }
}
