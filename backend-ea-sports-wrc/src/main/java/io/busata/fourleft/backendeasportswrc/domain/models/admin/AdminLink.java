package io.busata.fourleft.backendeasportswrc.domain.models.admin;

import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/** A private, expiring link to the operator pages; the UUID is the credential. */
@Entity
@Getter
@NoArgsConstructor
public class AdminLink {

    @Id
    @GeneratedValue
    private UUID id;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    public AdminLink(String createdBy, Duration validFor) {
        this.createdBy = createdBy;
        this.createdAt = ApplicationClock.now();
        this.expiresAt = this.createdAt.plus(validFor);
    }

    public boolean isValid() {
        return ApplicationClock.now().isBefore(expiresAt);
    }
}
