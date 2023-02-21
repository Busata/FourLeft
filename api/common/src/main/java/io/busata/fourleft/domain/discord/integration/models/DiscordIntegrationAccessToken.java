package io.busata.fourleft.domain.discord.integration.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class DiscordIntegrationAccessToken {

    @Id
    @GeneratedValue
    UUID id;

    @Column(name="user_name")
    String userName;

    @Column(name="access_token")
    String accessToken;

    @Column(name="refresh_token")
    String refreshToken;

    @Column(name="expire_date")
    LocalDateTime expireDate;

    @Column(name="scope")
    String scope;

    public DiscordIntegrationAccessToken(String userName, String accessToken, String refreshToken, LocalDateTime expireDate, String scope) {
        this.userName = userName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expireDate = expireDate;
        this.scope = scope;
    }

}
