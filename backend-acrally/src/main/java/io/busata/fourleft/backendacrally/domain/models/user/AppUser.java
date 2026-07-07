package io.busata.fourleft.backendacrally.domain.models.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor
public class AppUser {

    @Id
    private UUID id;

    /** Optional contact address, volunteered by the user — not a credential. */
    @Column
    @Setter
    private String email;

    @Column(name = "display_name", nullable = false)
    @Setter
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private UserRole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AppUser(String displayName) {
        this.id = UUID.randomUUID();
        this.displayName = displayName;
        this.status = UserStatus.ACTIVE;
        this.role = UserRole.USER;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}
