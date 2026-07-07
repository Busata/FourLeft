package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.AdminUserTo;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Administration surface for user management. The whole {@code /acrally-api/admin/**} tree is
 * locked to ROLE_ADMIN in {@code SecurityConfig}; the principal null-check here is defence in depth.
 */
@RestController
@RequestMapping("/acrally-api/admin/users")
@RequiredArgsConstructor
public class AdminUserEndpoint {

    private final AppUserRepository userRepository;

    @GetMapping("")
    public List<AdminUserTo> listUsers(@AuthenticationPrincipal AppUserDetails principal) {
        requireAdmin(principal);
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toAdminUser)
                .toList();
    }

    private AdminUserTo toAdminUser(AppUser user) {
        return new AdminUserTo(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus().name(),
                user.isAdmin(),
                user.getCreatedAt());
    }

    private void requireAdmin(AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
