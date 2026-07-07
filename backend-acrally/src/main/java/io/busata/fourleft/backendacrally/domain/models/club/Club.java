package io.busata.fourleft.backendacrally.domain.models.club;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** A user-created community club. The {@code createdBy} user is the owner/creator. */
@Entity
@Table(name = "club")
@Getter
@NoArgsConstructor
public class Club {

    @Id
    private UUID id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column
    @Setter
    private String description;

    @Column(name = "social_link")
    @Setter
    private String socialLink;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Club(String name, String description, String socialLink, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.socialLink = socialLink;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
}
