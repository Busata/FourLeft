package io.busata.fourleft.backendacrally.domain.models.agent;

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
@Table(name = "device_pairing")
@Getter
@NoArgsConstructor
public class DevicePairing {

    @Id
    private UUID id;

    @Column(name = "device_code_hash", nullable = false)
    private String deviceCodeHash;

    @Column(name = "user_code", nullable = false)
    private String userCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PairingStatus status;

    @Column(name = "label")
    private String label;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "api_key_id")
    private UUID apiKeyId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public DevicePairing(String deviceCodeHash, String userCode, String label, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.deviceCodeHash = deviceCodeHash;
        this.userCode = userCode;
        this.label = label;
        this.status = PairingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void approve(UUID userId) {
        this.status = PairingStatus.APPROVED;
        this.userId = userId;
        this.approvedAt = LocalDateTime.now();
    }

    public void deny() {
        this.status = PairingStatus.DENIED;
    }

    public void consume(UUID apiKeyId) {
        this.status = PairingStatus.CONSUMED;
        this.apiKeyId = apiKeyId;
    }
}
