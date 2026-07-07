package io.busata.fourleft.backendacrally.domain.models.championship;

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

/**
 * A championship scheduled by a club owner: a named season anchored on a start moment (date + time),
 * holding an ordered set of {@link ChampionshipEvent}s. The {@code createdBy} user is the club owner
 * who scheduled it.
 */
@Entity
@Table(name = "championship")
@Getter
@NoArgsConstructor
public class Championship {

    @Id
    private UUID id;

    @Column(name = "club_id", nullable = false, updatable = false)
    private UUID clubId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChampionshipStatus status;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Championship(UUID clubId, String name, LocalDateTime startsAt, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.clubId = clubId;
        this.name = name.strip();
        this.startsAt = startsAt;
        this.status = ChampionshipStatus.DRAFT;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String name, LocalDateTime startsAt, ChampionshipStatus status) {
        this.name = name.strip();
        this.startsAt = startsAt;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
