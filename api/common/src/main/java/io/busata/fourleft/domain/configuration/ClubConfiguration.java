package io.busata.fourleft.domain.configuration;

import io.busata.fourleft.domain.discord.models.AutomatedGenerationType;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Getter
@Table(name="club_configuration")
public class ClubConfiguration {
    @Id
    @GeneratedValue
    UUID id;

    String description;

    @Column(name="club_id")
    Long clubId;

    @Enumerated(EnumType.STRING)
    AutomatedGenerationType automatedGenerationType;

    public boolean isDaily() {
        return automatedGenerationType == AutomatedGenerationType.DAILY;
    }
    public boolean isMonthly() {
        return automatedGenerationType == AutomatedGenerationType.MONTHLY;
    }
}
