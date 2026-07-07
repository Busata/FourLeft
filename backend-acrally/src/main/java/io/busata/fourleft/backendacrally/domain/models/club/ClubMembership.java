package io.busata.fourleft.backendacrally.domain.models.club;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** A user's membership of a {@link Club}. Uniqueness is enforced per (club, user). */
@Entity
@Table(name = "club_membership")
@Getter
@NoArgsConstructor
public class ClubMembership {

    @Id
    private UUID id;

    @Column(name = "club_id", nullable = false)
    private UUID clubId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public ClubMembership(UUID clubId, UUID userId) {
        this.id = UUID.randomUUID();
        this.clubId = clubId;
        this.userId = userId;
        this.joinedAt = LocalDateTime.now();
    }
}
