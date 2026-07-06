package io.busata.fourleft.backendacrally.domain.models.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "linked_identity")
@Getter
@NoArgsConstructor
public class LinkedIdentity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentityProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    public LinkedIdentity(IdentityProvider provider, String providerUserId, UUID userId) {
        this.id = UUID.randomUUID();
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.userId = userId;
        this.linkedAt = LocalDateTime.now();
    }
}
