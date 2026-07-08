package io.busata.fourleft.backendacrally.infrastructure.security;

import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.models.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * spring-session-jdbc stores the session's SecurityContext in Postgres via JDK
 * serialization (spring_session_attributes.attribute_bytes). If AppUserDetails ever
 * becomes non-serializable, every login breaks at session save — this pins the round-trip.
 */
class AppUserDetailsSerializationTest {

    @Test
    void securityContextWithAppUserDetailsSurvivesJdkSerialization() throws Exception {
        AppUser user = new AppUser("Tester");
        user.setRole(UserRole.ADMIN);
        AppUserDetails details = new AppUserDetails(user);
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                details, null, details.getAuthorities());
        SecurityContext context = new SecurityContextImpl(auth);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(context);
        }
        SecurityContext restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
            restored = (SecurityContext) in.readObject();
        }

        AppUserDetails principal = (AppUserDetails) restored.getAuthentication().getPrincipal();
        assertThat(principal.getId()).isEqualTo(details.getId());
        assertThat(principal.getDisplayName()).isEqualTo("Tester");
        assertThat(principal.isAdmin()).isTrue();
        assertThat(restored.getAuthentication().getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
