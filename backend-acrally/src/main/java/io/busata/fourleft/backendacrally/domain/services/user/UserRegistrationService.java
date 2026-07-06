package io.busata.fourleft.backendacrally.domain.services.user;

import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    // Deliberately loose — a real deliverability check is out of scope; this just
    // rejects obvious garbage before we hash a password for it.
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AppUser register(String rawEmail, String rawPassword, String rawDisplayName) {
        final String email = rawEmail == null ? "" : rawEmail.trim();
        final String displayName = rawDisplayName == null ? "" : rawDisplayName.trim();
        final String password = rawPassword == null ? "" : rawPassword;

        if (!EMAIL.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid email address is required.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least %d characters.".formatted(MIN_PASSWORD_LENGTH));
        }
        if (displayName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A display name is required.");
        }

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        if (repository.existsByDisplayNameIgnoreCase(displayName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This display name is taken.");
        }

        return repository.save(new AppUser(email, passwordEncoder.encode(password), displayName));
    }
}
