package io.busata.fourleft.backendacrally.domain.services.user;

import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    /** Matches the app_user.display_name column width (V001). */
    private static final int MAX_DISPLAY_NAME_LENGTH = 60;

    private final AppUserRepository repository;

    /**
     * Renames the account. Display names stay unique case-insensitively (the cheap
     * impersonation guard from V001); the Steam identity, not the name, is who you are.
     */
    @Transactional
    public AppUser changeDisplayName(UUID userId, String rawDisplayName) {
        final String displayName = rawDisplayName == null ? "" : rawDisplayName.trim();
        if (displayName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A display name is required.");
        }
        if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Display name can be at most %d characters.".formatted(MAX_DISPLAY_NAME_LENGTH));
        }

        AppUser user = repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (displayName.equalsIgnoreCase(user.getDisplayName())) {
            // Case-only tweak of your own name is always allowed.
            user.setDisplayName(displayName);
            return user;
        }
        if (repository.existsByDisplayNameIgnoreCase(displayName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This display name is taken.");
        }
        user.setDisplayName(displayName);
        return user;
    }
}
